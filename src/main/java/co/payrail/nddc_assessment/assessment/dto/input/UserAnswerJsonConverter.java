package co.payrail.nddc_assessment.assessment.dto.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class UserAnswerJsonConverter implements AttributeConverter<List<UserAnswer>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<UserAnswer> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            // Handle the exception (log it, etc.)
            throw new IllegalArgumentException("Error converting list of UserAnswer to JSON", e);
        }
    }

    @Override
    public List<UserAnswer> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, UserAnswer.class));
        } catch (IOException e) {
            // Handle the exception (log it, etc.)
            throw new IllegalArgumentException("Error converting JSON to list of UserAnswer", e);
        }
    }
}
