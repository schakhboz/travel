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
}
