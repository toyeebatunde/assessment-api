package co.payrail.nddc_assessment.assessment.entity;

import co.payrail.nddc_assessment.users.entity.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_assessment_answers")
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;  // Link to the user who provided the answer


    @ManyToOne
    private Question question;

    @ElementCollection
    @CollectionTable(name = "selected_options")
    private List<Option> selectedOptions;  // For MCQ

    private String shortAnswer;  // For short answer questions
}

