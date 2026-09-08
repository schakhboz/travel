package uz.insonline.travel.CentrumAir.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.error.CentrumAirErrorCode;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyEntity;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyPolicyEntity;
import uz.insonline.travel.CentrumAir.idempotency.repository.InsCentrumAirIdempotencyPolicyRepository;
import uz.insonline.travel.CentrumAir.idempotency.repository.InsCentrumAirIdempotencyRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Идемпотентность выпуска (ТЗ п. 7.3):
 * <ul>
 *   <li>ключ — заголовок {@code Idempotency-Key}, а если его нет — PNR + набор продуктов + номер попытки оплаты;</li>
 *   <li>повтор с тем же ключом не создаёт дублей: возвращается результат первичного выпуска;</li>
 *   <li>после частичного отказа повтор довыпускает только недостающие полисы;</li>
 *   <li>активная заявка на ту же бронь и продукты без явного ключа — ошибка {@code duplicate} (ТЗ п. 7.6.5).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final EnumSet<IdempotencyStatus> LIVE_STATUSES =
            EnumSet.of(IdempotencyStatus.IN_PROGRESS, IdempotencyStatus.PARTIAL, IdempotencyStatus.COMPLETED);

    private final InsCentrumAirIdempotencyRepository idempotencyRepository;
    private final InsCentrumAirIdempotencyPolicyRepository issuedPolicyRepository;
    private final CentrumAirProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Регистрирует попытку выпуска и сообщает, что с ней делать: вернуть сохранённый ответ,
     * довыпустить недостающие полисы или выпускать с нуля.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord begin(PolicyIssueRequest request, String headerKey) {
        ProductSelection products = ProductSelection.of(request.products());
        String fingerprint = products.fingerprint();
        String key = resolveKey(request, headerKey, fingerprint);
        String requestHash = hash(serialize(request));

        Optional<InsCentrumAirIdempotencyEntity> existing = idempotencyRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return resume(existing.get(), requestHash);
        }

        if (isBlank(headerKey)) {
            requireNoActiveDuplicate(request.pnr(), fingerprint);
        }

        try {
            InsCentrumAirIdempotencyEntity entity = new InsCentrumAirIdempotencyEntity();
            entity.setIdempotencyKey(key);
            entity.setPnr(request.pnr());
            entity.setProductFingerprint(fingerprint);
            entity.setRequestHash(requestHash);
            entity.setStatus(IdempotencyStatus.IN_PROGRESS);
            InsCentrumAirIdempotencyEntity saved = idempotencyRepository.saveAndFlush(entity);
            log.info("Issue request accepted: pnr={}, idempotencyKey={}", request.pnr(), key);
            return new IdempotencyRecord(saved.getId(), key, null, null, Map.of());
        } catch (DataIntegrityViolationException raceLost) {
            // Параллельный запрос с тем же ключом успел первым — работаем с его записью.
            return resume(idempotencyRepository.findByIdempotencyKey(key).orElseThrow(() -> raceLost), requestHash);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordBooking(Long recordId, Long bookingId) {
        InsCentrumAirIdempotencyEntity entity = idempotencyRepository.getReferenceById(recordId);
        entity.setBookingId(bookingId);
    }

    /** Фиксирует выпущенный полис группы сразу после его выпуска — до завершения всей заявки. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIssuedGroup(Long recordId, int policyGroup, Long contractId, Long policyId) {
        InsCentrumAirIdempotencyPolicyEntity issued = new InsCentrumAirIdempotencyPolicyEntity();
        issued.setIdempotencyId(recordId);
        issued.setPolicyGroup(policyGroup);
        issued.setContractId(contractId);
        issued.setPolicyId(policyId);
        issuedPolicyRepository.save(issued);

        InsCentrumAirIdempotencyEntity entity = idempotencyRepository.getReferenceById(recordId);
        entity.setStatus(IdempotencyStatus.PARTIAL);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long recordId, PolicyIssueResponse response) {
        InsCentrumAirIdempotencyEntity entity = idempotencyRepository.getReferenceById(recordId);
        entity.setStatus(IdempotencyStatus.COMPLETED);
        entity.setResponseJson(serialize(response));
        entity.setErrorMessage(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long recordId, String errorMessage) {
        InsCentrumAirIdempotencyEntity entity = idempotencyRepository.getReferenceById(recordId);
        entity.setStatus(IdempotencyStatus.FAILED);
        entity.setErrorMessage(truncate(errorMessage));
    }

    private IdempotencyRecord resume(InsCentrumAirIdempotencyEntity entity, String requestHash) {
        if (!entity.getRequestHash().equals(requestHash)) {
            throw new CentrumAirApiException(CentrumAirErrorCode.IDEMPOTENCY_KEY_REUSE,
                    "Idempotency key is already used for a different request payload");
        }
        if (entity.getStatus() == IdempotencyStatus.COMPLETED) {
            log.info("Replaying stored issue result for idempotencyKey={}", entity.getIdempotencyKey());
            return new IdempotencyRecord(entity.getId(), entity.getIdempotencyKey(),
                    deserialize(entity.getResponseJson()), entity.getBookingId(), issuedGroups(entity.getId()));
        }
        if (entity.getStatus() == IdempotencyStatus.IN_PROGRESS && isFresh(entity)) {
            throw new CentrumAirApiException(CentrumAirErrorCode.ISSUE_IN_PROGRESS,
                    "Issue for this idempotency key is still in progress, retry later");
        }

        Map<Integer, IdempotencyRecord.IssuedGroup> issued = issuedGroups(entity.getId());
        entity.setStatus(IdempotencyStatus.IN_PROGRESS);
        log.info("Resuming issue for idempotencyKey={}, already issued groups={}",
                entity.getIdempotencyKey(), issued.keySet());
        return new IdempotencyRecord(entity.getId(), entity.getIdempotencyKey(), null, entity.getBookingId(), issued);
    }

    private void requireNoActiveDuplicate(String pnr, String fingerprint) {
        if (idempotencyRepository.existsByPnrAndProductFingerprintAndStatusIn(pnr, fingerprint, LIVE_STATUSES)) {
            throw new CentrumAirApiException(CentrumAirErrorCode.DUPLICATE,
                    "Active policies already exist for PNR " + pnr + " and the same products; "
                            + "send an explicit Idempotency-Key header to issue them again");
        }
    }

    private Map<Integer, IdempotencyRecord.IssuedGroup> issuedGroups(Long recordId) {
        List<InsCentrumAirIdempotencyPolicyEntity> rows =
                issuedPolicyRepository.findByIdempotencyIdOrderByPolicyGroup(recordId);
        Map<Integer, IdempotencyRecord.IssuedGroup> groups = new LinkedHashMap<>();
        for (InsCentrumAirIdempotencyPolicyEntity row : rows) {
            groups.put(row.getPolicyGroup(), new IdempotencyRecord.IssuedGroup(row.getContractId(), row.getPolicyId()));
        }
        return groups;
    }

    private boolean isFresh(InsCentrumAirIdempotencyEntity entity) {
        OffsetDateTime deadline = entity.getUpdatedAt().plus(properties.getIdempotency().getInProgressTimeout());
        return OffsetDateTime.now().isBefore(deadline);
    }

    private String resolveKey(PolicyIssueRequest request, String headerKey, String fingerprint) {
        if (!isBlank(headerKey)) {
            String key = headerKey.trim();
            if (key.length() > 255) {
                throw CentrumAirApiException.validation("Idempotency-Key must not exceed 255 characters");
            }
            return key;
        }
        return hash(String.join("|", request.pnr(), fingerprint, String.valueOf(request.paymentAttemptOrFirst())));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency payload", e);
        }
    }

    private PolicyIssueResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, PolicyIssueResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read stored issue response", e);
        }
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
