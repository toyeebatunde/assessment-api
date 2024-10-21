package co.payrail.nddc_assessment.assessment.repository;

import co.payrail.nddc_assessment.assessment.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {}
