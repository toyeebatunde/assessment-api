package co.payrail.nddc_assessment.assessment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;  // Import for JSON mapping
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable  // Option should be marked as embeddable, not an entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    @JsonProperty("optionText")  // Ensures proper mapping for JSON input
    private String optionText;

    @JsonProperty("isCorrect")  // Ensures 'isCorrect' from JSON maps correctly
    private boolean isCorrect;  // Whether it's the correct answer
}
