package co.payrail.nddc_assessment.assessment.dto.input;

import co.payrail.nddc_assessment.assessment.entity.Option;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;

// UserAnswer.java
@Data
@Embeddable
public class UserAnswer {
    @JsonProperty("questionId")
    private Long questionId;  // Links to the specific question
    @JsonProperty("selectedOptions")
    private List<Option> selectedOptions;  // For MCQ and True/False
    private String shortAnswer;  // For Short Answer type questions
}
