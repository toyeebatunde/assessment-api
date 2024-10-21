package co.payrail.nddc_assessment.users.dto.input;

import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String name;
    private String location;
    private int age;
    private String gender;
    private String religion;
    private String disabilityStatus;

    // Getters and Setters
}

