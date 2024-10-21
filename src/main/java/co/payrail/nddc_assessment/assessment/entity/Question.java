package co.payrail.nddc_assessment.assessment.entity;

import co.payrail.nddc_assessment.assessment.dto.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assessment_questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionText;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;  // ENUM for MCQ, True/False, etc.

    // Correctly embedding a list of options using @ElementCollection
    @ElementCollection
    @CollectionTable(name = "question_options")
    private List<Option> options;  // For MCQs or True/False

    private String correctAnswer;  // For Short Answer type

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;  // Direct reference to the assessment ID

    private int awardedScore = 1;
}
