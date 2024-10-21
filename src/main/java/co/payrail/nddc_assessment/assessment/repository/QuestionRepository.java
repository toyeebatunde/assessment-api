package co.payrail.nddc_assessment.assessment.repository;

import co.payrail.nddc_assessment.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Method to find questions by assessment ID
    List<Question> findByAssessmentId(Long assessmentId);
}
