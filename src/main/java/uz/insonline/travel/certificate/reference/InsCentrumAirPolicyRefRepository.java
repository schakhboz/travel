package uz.insonline.travel.certificate.reference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsCentrumAirPolicyRefRepository extends JpaRepository<InsCentrumAirPolicyRefEntity, Long> {

    Optional<InsCentrumAirPolicyRefEntity> findByRiskCode(String riskCode);
}
