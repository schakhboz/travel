package uz.insonline.travel.authentication.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.authentication.entity.Policy;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByAnketaIdAndUserId(Long anketaId, Long userId);

    Optional<Policy> findByPolicySeryAndPolicyNumber(String policySery, Long policyNumber);

    Optional<Policy> findByTbIdAndAnketaIdAndStatus(Long tbId, Long anketaId, Long status);

    @Query(value = """
            SELECT REGEXP_REPLACE(COALESCE(k.TB_PHONE1, k.TB_PHONE2, k.TB_PHONE3), '[^0-9]', '')
            FROM ins_kontragent k
            INNER JOIN INS_ANKETA a ON a.owner = k.tb_id
            WHERE a.ins_id = :contractId
            """, nativeQuery = true)
    Optional<String> findPhoneNumberByContractId(@Param("contractId") Long contractId);
}
