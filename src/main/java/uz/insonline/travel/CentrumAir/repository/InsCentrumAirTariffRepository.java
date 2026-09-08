package uz.insonline.travel.CentrumAir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirTariffEntity;

import java.util.List;

@Repository
public interface InsCentrumAirTariffRepository extends JpaRepository<InsCentrumAirTariffEntity, Long> {

    List<InsCentrumAirTariffEntity> findByPolicyGroupAndTariffCodeIn(Integer policyGroup, List<String> tariffCodes);

    @Query("SELECT t FROM InsCentrumAirTariffEntity t WHERE t.policyGroup = :policyGroup")
    List<InsCentrumAirTariffEntity> findByPolicyGroup(@Param("policyGroup") Integer policyGroup);

    List<InsCentrumAirTariffEntity> findByTariffCodeAndRiskRiskCode(String tariffCode, String riskCode);

    /** Тарифная матрица для справочника: риск подгружается сразу, без отдельного запроса на строку. */
    @Query("SELECT t FROM InsCentrumAirTariffEntity t JOIN FETCH t.risk r "
            + "WHERE (:policyGroup IS NULL OR t.policyGroup = :policyGroup) "
            + "AND (:tariffCode IS NULL OR UPPER(t.tariffCode) = :tariffCode) "
            + "ORDER BY t.policyGroup, t.tariffCode, r.riskCode")
    List<InsCentrumAirTariffEntity> findForDictionary(@Param("policyGroup") Integer policyGroup,
                                                      @Param("tariffCode") String tariffCode);
}
