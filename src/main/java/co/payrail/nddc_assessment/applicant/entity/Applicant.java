package co.payrail.nddc_assessment.applicant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "InternDetails")
@Data
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "FirstName")
    private String firstName;
    @Column(name = "LastName")
    private String lastName;
    @Column(name = "InternCode")
    private String internCode;
    @Column(name = "Gender")
    private String gender;
    @Column(name = "DateOfBirth")
    private String dateOfBirth;
    @Column(name = "Interest")
    private String interest;
    @Column(name = "EducationLevel")
    private String educationLevel;
    @Column(name = "Phone1")
    private String phoneNumber;
    @Column(name = "RegistrationEmail")
    private String email;
    @Column(name = "AddressState")
    private String state;
    @Column(name = "StateOfOrigin")
    private String stateOfOrigin;
    @Column(name = "OriginLGA")
    private String originLGA;
    @Column(name = "Shortlisted")
    private String shortlisted;


}
