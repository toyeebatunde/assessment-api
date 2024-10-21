package co.payrail.nddc_assessment.assessment.dto.input;

import co.payrail.nddc_assessment.assessment.dto.enums.QuestionType;
import co.payrail.nddc_assessment.assessment.entity.Option;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private String questionText;  // The actual question text
    private QuestionType questionType;  // The type of the question (MCQ, TRUE_FALSE, etc.)

    // For MCQ and MULTI_CORRECT, we provide options
    private List<Option> options;

    // For SHORT_ANSWER, we provide the correct answer
    private String correctAnswer;

    // For TRUE_FALSE, we specify if "True" is the correct answer
    private boolean correctTrueFalse;
}
