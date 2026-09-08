package uz.insonline.travel.certificate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.certificate.entity.InsCertificateEntity;

import java.util.Optional;

@Repository
public interface InsCertificateRepository extends JpaRepository<InsCertificateEntity, Long> {

    Optional<InsCertificateEntity> findByBookingId(Long bookingId);

    Optional<InsCertificateEntity> findByCertificateNumber(String certificateNumber);
}
