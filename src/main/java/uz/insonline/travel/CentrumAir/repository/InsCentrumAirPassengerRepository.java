package uz.insonline.travel.CentrumAir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.entity.InsCentrumAirPassengerEntity;

import java.util.List;

@Repository
public interface InsCentrumAirPassengerRepository extends JpaRepository<InsCentrumAirPassengerEntity, Long> {

    List<InsCentrumAirPassengerEntity> findByBookingId(Long bookingId);

    List<InsCentrumAirPassengerEntity> findByContractId(Long contractId);
}