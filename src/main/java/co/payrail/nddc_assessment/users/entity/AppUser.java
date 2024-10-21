package co.payrail.nddc_assessment.users.entity;

import co.payrail.nddc_assessment.assessment.dto.enums.AssessmentStatus;
import co.payrail.nddc_assessment.assessment.dto.input.UserAnswer;
import co.payrail.nddc_assessment.assessment.dto.input.UserAnswerJsonConverter;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.entity.Option;
import co.payrail.nddc_assessment.users.dto.enums.DisabilityStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assessment_taker")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String userName;
    private String name;
    private int age;
    private String location;
    private String gender;
    private String religion;
    private String interest;
    private DisabilityStatus disabilityStatus;
    private String educationLevel;
    private String phoneNumber;
    private String email;
    private String InternCode;
    private String state;
    private String stateOfOrigin;
    private String originLGA;
    private String addressLGA;
    @Enumerated(EnumType.STRING)
    private AssessmentStatus assessmentStatus = AssessmentStatus.PENDING; // Tracks user progress (PENDING, STARTED, COMPLETED)
    private Long assessmentId;
    private int score;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = UserAnswerJsonConverter.class)
    private List<UserAnswer> answers;


    @Column( nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean selectionStatus = false;
}
