package co.payrail.nddc_assessment.applicant.service;

import co.payrail.nddc_assessment.applicant.entity.Applicant;
import co.payrail.nddc_assessment.applicant.repository.ApplicantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepository;

    private static final int BATCH_SIZE = 1000;

    public List<Applicant> fetchApplicants(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
        Page<Applicant> applicants = applicantRepository.findApplicantByShortlisted("true",pageable);
        return applicants.getContent();
    }
}
