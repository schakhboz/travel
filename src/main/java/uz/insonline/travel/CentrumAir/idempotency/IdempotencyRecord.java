package uz.insonline.travel.CentrumAir.idempotency;

import uz.insonline.travel.CentrumAir.dto.response.PolicyIssueResponse;

import java.util.Map;

/**
 * Состояние заявки на выпуск для текущего запроса.
 *
 * @param replay       ответ первичного выпуска, если заявка уже завершена — его и надо вернуть
 * @param issuedGroups учётные группы, полисы по которым уже выпущены: их повтор не трогает
 */
public record IdempotencyRecord(
        Long id,
        String key,
        PolicyIssueResponse replay,
        Long bookingId,
        Map<Integer, IssuedGroup> issuedGroups
) {

    public boolean isReplay() {
        return replay != null;
    }

    public record IssuedGroup(Long contractId, Long policyId) {
    }
}
