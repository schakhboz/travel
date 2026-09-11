package uz.insonline.travel.CentrumAir.idempotency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyEntity;

import java.util.Optional;

@Repository
public interface InsCentrumAirIdempotencyRepository extends JpaRepository<InsCentrumAirIdempotencyEntity, Long> {

    Optional<InsCentrumAirIdempotencyEntity> findByIdempotencyKey(String idempotencyKey);
}
