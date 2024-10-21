package co.payrail.nddc_assessment.users.service.implementation;

import co.payrail.nddc_assessment.users.dto.enums.DisabilityStatus;
import org.springframework.data.jpa.domain.Specification;
import co.payrail.nddc_assessment.users.entity.AppUser;

public class AppUserSpecification {

    public static Specification<AppUser> hasAssessmentId(Long assessmentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assessmentId"), assessmentId);
    }
    public static Specification<AppUser> hasScoreGreaterThan(int score) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("score"), score);
    }

    public static Specification<AppUser> hasState(String state) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), state);
    }

    public static Specification<AppUser> hasAddressLGA(String lga) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("addressLGA"), lga);
    }

    public static Specification<AppUser> hasGender(String gender) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("gender"), gender);
    }

    public static Specification<AppUser> hasAgeGreaterThanOrEqual(Integer age) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("age"), age);
    }

    public static Specification<AppUser> hasReligion(String religion) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("religion"), religion);
    }

    public static Specification<AppUser> hasInterest(String interest) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("interest"), interest);
    }

    public static Specification<AppUser> hasDisabilityStatus(DisabilityStatus disabilityStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("disabilityStatus"), disabilityStatus);
    }

    public static Specification<AppUser> hasEducationLevel(String educationLevel) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("educationLevel"), educationLevel);
    }

    // New method to filter based on the selectionStatus field
    public static Specification<AppUser> hasSelectionStatus(Boolean selectionStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("selectionStatus"), selectionStatus);
    }
    // Add more specifications for other filters like age, religion, etc.
}
