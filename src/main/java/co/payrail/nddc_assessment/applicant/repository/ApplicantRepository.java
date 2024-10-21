package co.payrail.nddc_assessment.applicant.repository;

import co.payrail.nddc_assessment.applicant.entity.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant,Long> {
    Page<Applicant> findAll(Pageable pageable);
    Page<Applicant> findApplicantByShortlisted(String shortlisted,Pageable pageable);
}
