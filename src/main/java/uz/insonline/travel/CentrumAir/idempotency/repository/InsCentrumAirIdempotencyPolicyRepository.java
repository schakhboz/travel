package uz.insonline.travel.CentrumAir.idempotency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.idempotency.entity.InsCentrumAirIdempotencyPolicyEntity;

import java.util.List;

@Repository
public interface InsCentrumAirIdempotencyPolicyRepository
        extends JpaRepository<InsCentrumAirIdempotencyPolicyEntity, Long> {

    List<InsCentrumAirIdempotencyPolicyEntity> findByIdempotencyIdOrderByPolicyGroup(Long idempotencyId);
}
