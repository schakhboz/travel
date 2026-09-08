package uz.insonline.travel.CentrumAir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.insonline.travel.CentrumAir.config.CentrumAirProperties;
import uz.insonline.travel.CentrumAir.dto.IssueContentDto;
import uz.insonline.travel.CentrumAir.dto.PolicyItemDto;
import uz.insonline.travel.CentrumAir.dto.ProductDto;
import uz.insonline.travel.CentrumAir.dto.request.PolicyIssueRequest;
import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;
import uz.insonline.travel.CentrumAir.error.CentrumAirErrorCode;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyRecord;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyService;
import uz.insonline.travel.CentrumAir.idempotency.IdempotencyStatus;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyEntity;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyPolicyEntity;
import uz.insonline.travel.CentrumAir.idempotency.repository.InsCentrumAirIdempotencyPolicyRepository;
import uz.insonline.travel.CentrumAir.idempotency.repository.InsCentrumAirIdempotencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final List<ProductDto> PRODUCTS = List.of(new ProductDto("STANDARD", null));

    @Mock
    private InsCentrumAirIdempotencyRepository idempotencyRepository;

    @Mock
    private InsCentrumAirIdempotencyPolicyRepository issuedPolicyRepository;

    @Spy
    private CentrumAirProperties properties = new CentrumAirProperties();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private IdempotencyService service;

    private PolicyIssueRequest request;

    @BeforeEach
    void setUp() {
        request = PolicyRequests.issueRequest("PNR123", PRODUCTS);
    }

    @Test
    void firstRequestStartsNewIssue() {
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(idempotencyRepository.existsByPnrAndProductFingerprintAndStatusIn(anyString(), anyString(), any()))
                .thenReturn(false);
        when(idempotencyRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            InsCentrumAirIdempotencyEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        IdempotencyRecord record = service.begin(request, null);

        assertFalse(record.isReplay());
        assertEquals(1L, record.id());
        assertTrue(record.issuedGroups().isEmpty());
    }

    @Test
    void repeatOfCompletedRequestReplaysStoredResponse() throws Exception {
        PolicyIssueResponse stored = PolicyIssueResponse.success(List.of(new IssueContentDto(
                "PNR123", LocalDate.of(2026, 9, 1), "Ivan Ivanov", new BigDecimal("500000"), "UZS",
                List.of(new PolicyItemDto(1, 10L, 20L, "EIND", "555", "555", "ISSUED",
                        new BigDecimal("500000"), new BigDecimal("105000000"), List.of("ACCIDENT"),
                        "2026-09-04", "2026-09-11", null)))));
        InsCentrumAirIdempotencyEntity entity = existing(IdempotencyStatus.COMPLETED);
        entity.setResponseJson(objectMapper.writeValueAsString(stored));
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(entity));

        IdempotencyRecord record = service.begin(request, "key-1");

        assertTrue(record.isReplay());
        assertEquals("555", record.replay().content().get(0).policies().get(0).policyNumber());
        verify(idempotencyRepository, never()).saveAndFlush(any());
    }

    @Test
    void repeatAfterPartialFailureResumesMissingGroupsOnly() {
        InsCentrumAirIdempotencyEntity entity = existing(IdempotencyStatus.PARTIAL);
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(entity));
        when(issuedPolicyRepository.findByIdempotencyIdOrderByPolicyGroup(1L))
                .thenReturn(List.of(issuedPolicy(1, 100L, 200L)));

        IdempotencyRecord record = service.begin(request, "key-1");

        assertFalse(record.isReplay());
        assertEquals(1, record.issuedGroups().size());
        assertEquals(100L, record.issuedGroups().get(1).contractId());
        assertEquals(IdempotencyStatus.IN_PROGRESS, entity.getStatus());
    }

    @Test
    void sameKeyWithDifferentPayloadIsRejected() {
        InsCentrumAirIdempotencyEntity entity = existing(IdempotencyStatus.COMPLETED);
        entity.setRequestHash("0".repeat(64));
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(entity));

        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> service.begin(request, "key-1"));
        assertEquals(CentrumAirErrorCode.IDEMPOTENCY_KEY_REUSE, error.getErrorCode());
    }

    @Test
    void concurrentIssueWithSameKeyIsRejected() {
        InsCentrumAirIdempotencyEntity entity = existing(IdempotencyStatus.IN_PROGRESS);
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(entity));

        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> service.begin(request, "key-1"));
        assertEquals(CentrumAirErrorCode.ISSUE_IN_PROGRESS, error.getErrorCode());
    }

    @Test
    void secondPaymentAttemptWithoutHeaderKeyIsDuplicate() {
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(idempotencyRepository.existsByPnrAndProductFingerprintAndStatusIn(anyString(), anyString(), any()))
                .thenReturn(true);

        CentrumAirApiException error = assertThrows(CentrumAirApiException.class,
                () -> service.begin(request, null));
        assertEquals(CentrumAirErrorCode.DUPLICATE, error.getErrorCode());
    }

    @Test
    void derivedKeyChangesWithPaymentAttempt() {
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(idempotencyRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.begin(request, null);
        String firstAttemptKey = capturedKey();

        clearInvocations(idempotencyRepository);
        when(idempotencyRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(idempotencyRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.begin(retry(request), null);

        assertNotEquals(firstAttemptKey, capturedKey());
    }

    private String capturedKey() {
        org.mockito.ArgumentCaptor<InsCentrumAirIdempotencyEntity> captor =
                org.mockito.ArgumentCaptor.forClass(InsCentrumAirIdempotencyEntity.class);
        verify(idempotencyRepository).saveAndFlush(captor.capture());
        return captor.getValue().getIdempotencyKey();
    }

    private PolicyIssueRequest retry(PolicyIssueRequest source) {
        return new PolicyIssueRequest(source.pnr(), source.paymentTime(), source.salesChannel(),
                source.totalPremiumAmount(), source.premiumCurrency(), source.route(), source.language(),
                source.products(), source.transactions(), source.insurant(), source.passengers(), 2);
    }

    private InsCentrumAirIdempotencyEntity existing(IdempotencyStatus status) {
        InsCentrumAirIdempotencyEntity entity = new InsCentrumAirIdempotencyEntity();
        entity.setId(1L);
        entity.setIdempotencyKey("key-1");
        entity.setPnr("PNR123");
        entity.setStatus(status);
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setRequestHash(requestHash());
        return entity;
    }

    /** Хэш считается тем же способом, что и в сервисе: сериализация запроса тем же ObjectMapper. */
    private String requestHash() {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(objectMapper.writeValueAsString(request).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private InsCentrumAirIdempotencyPolicyEntity issuedPolicy(int group, Long contractId, Long policyId) {
        InsCentrumAirIdempotencyPolicyEntity entity = new InsCentrumAirIdempotencyPolicyEntity();
        entity.setPolicyGroup(group);
        entity.setContractId(contractId);
        entity.setPolicyId(policyId);
        return entity;
    }
}
