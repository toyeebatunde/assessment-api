package co.payrail.nddc_assessment.users.service.implementation;

import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final UserRepository appUserRepository;

    public AnalyticsService(UserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Map<String, Long> getPassedScoresByNDDCStates(int passingScore, Long assessmentId) {
        List<AppUser> passedUsers = appUserRepository.findAll(
                Specification.where(AppUserSpecification.hasScoreGreaterThan(passingScore))
                        .and(AppUserSpecification.hasAssessmentId(assessmentId))
        );

        // Group by state and count users
        return passedUsers.stream()
                .collect(Collectors.groupingBy(AppUser::getState, Collectors.counting()));
    }

}
