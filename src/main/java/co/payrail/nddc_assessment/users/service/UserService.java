package co.payrail.nddc_assessment.users.service;


import co.payrail.nddc_assessment.users.dto.enums.DisabilityStatus;
import co.payrail.nddc_assessment.users.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService{

    @Transactional
    UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException;

    Optional<AppUser> findByUserName(String userName);

    Optional<AppUser> findAppUserById(Long id);

     AppUser createUserForAssessment(Long assessmentId, AppUser appUser);

    AppUser createBatchUserForAssessment(Long assessmentId, List<AppUser> appUser);

    @Transactional
    void saveUsersInBatch(List<AppUser> users);

    void startAssessment(Long userId);


    void save(AppUser user);

    List<AppUser> filterAppUsers( Long assessmentId, Integer score, String state,String lga, String gender, Integer minAge,
                                 String religion,
                                  String interest,
                                 DisabilityStatus disabilityStatus,
                                 Boolean selectionStatus,
                                 String educationLevel);

    Page<AppUser> filterAppUsers(
            Long assessmentId,
            Integer score,
            String state,
            String lga,
            String gender,
            Integer minAge,
            String religion,
            String interest,
            DisabilityStatus disabilityStatus,
            String educationLevel,
           Boolean selectionStatus,
            Pageable pageable);

    @Transactional
    void batchUpdateSelectionStatus(
            Long assessmentId,
            Integer score,
            String state,
            String lga,
            String gender,
            Integer minAge,
            String religion,
            String interest,
            DisabilityStatus disabilityStatus,
            String educationLevel,
            Pageable pageable);

    AppUser updateUser(AppUser userDTO);

    // Retrieve user by ID
    Optional<AppUser> getUserById(Long id);

    // Update user details with partial updates
    AppUser updateUser(Long id, AppUser updatedUser);

    void validateToken(String token, HttpServletRequest request) throws UnsupportedEncodingException;

    void navigateToPage(String token, String url, HttpServletResponse response) throws IOException;
}
