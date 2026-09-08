package uz.insonline.travel.CentrumAir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.CentrumAir.entity.InsPsugurtaPoObyektamEntity;

@Repository
public interface InsPsugurtaPoObyektamRepository extends JpaRepository<InsPsugurtaPoObyektamEntity, Long> {
}
