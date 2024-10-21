package co.payrail.nddc_assessment.users.repository;

import co.payrail.nddc_assessment.users.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByUserName(String userName);

    // Find users by assessmentId
    List<AppUser> findByAssessmentId(Long assessmentId);

    Page<AppUser> findAll(Specification<AppUser> spec, Pageable pageable);

    // Find specific user by email and assessment
    Optional<AppUser> findByUserNameAndAssessmentId(String userName, Long assessmentId);

    @Query("SELECT u.state, COUNT(u) FROM AppUser u WHERE u.score >= :passingScore GROUP BY u.state")
    List<Object[]> countPassedUsersByState(@Param("passingScore") int passingScore);
}
