package uz.insonline.travel.CentrumAir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirRiskEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsCentrumAirRiskRepository extends JpaRepository<InsCentrumAirRiskEntity, Long> {

    Optional<InsCentrumAirRiskEntity> findByRiskCode(String riskCode);

    List<InsCentrumAirRiskEntity> findAllByRiskCode(String riskCode);

    List<InsCentrumAirRiskEntity> findAllByOrderByRiskCodeAsc();
}
