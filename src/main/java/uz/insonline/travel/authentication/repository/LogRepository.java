package uz.insonline.travel.authentication.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.insonline.travel.log.entity.LogEntity;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Integer> {
}
