package co.payrail.nddc_assessment.users.controller;

import co.payrail.nddc_assessment.config.TokenProvider;
import co.payrail.nddc_assessment.users.dto.enums.DisabilityStatus;
import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.service.UserService;
import co.payrail.nddc_assessment.users.service.implementation.AnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app-users")
public class AppUserController {

    private final UserService appUserService;
    private final AnalyticsService analyticsService;

    private final TokenProvider tokenProvider;

    public AppUserController(UserService appUserService, AnalyticsService analyticsService, TokenProvider tokenProvider) {
        this.appUserService = appUserService;
        this.analyticsService = analyticsService;
        this.tokenProvider = tokenProvider;
    }

    // Retrieve user by ID
    @GetMapping("/get")
    public ResponseEntity<AppUser> getUserById() {
        return appUserService.getUserById(tokenProvider.getId())
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Update user details
    @PutMapping("/update")
    public ResponseEntity<AppUser> updateUser(@RequestBody AppUser updatedUser) {
        try {
            AppUser user = appUserService.updateUser(tokenProvider.getId(), updatedUser);
            return ResponseEntity.ok().body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter")
    public List<AppUser> filterAppUsers(
            @RequestParam(required = false) Long assessmentId,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false) DisabilityStatus disabilityStatus,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) Boolean selectionStatus) {
        return appUserService.filterAppUsers(assessmentId, score, state, lga, gender, minAge, religion,interest, disabilityStatus,selectionStatus, educationLevel);
    }

    @GetMapping("/paginated/filter")
    public Page<AppUser> filterPaginatedAppUsers(
            @RequestParam(required = false) Long assessmentId,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false) DisabilityStatus disabilityStatus,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) Boolean selectionStatus,
            @RequestParam(defaultValue = "0") int page,     // page number
            @RequestParam(defaultValue = "100") int size) {  // limit of data

        // Create a pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);

        // Call the service method
        return appUserService.filterAppUsers(
                assessmentId,
                score, state, lga, gender, minAge, religion,interest, disabilityStatus, educationLevel, selectionStatus, pageable);
    }

    @GetMapping("/analytics/passed-by-nddc-states")
    public Map<String, Long> getPassedScoresByNDDCStates(@RequestParam int passingScore, @RequestParam(required = false) Long assessmentId) {
        return analyticsService.getPassedScoresByNDDCStates(passingScore, assessmentId);
    }

    // Endpoint to batch update selection status to true for filtered users
    @PostMapping("/batch-update-selection-status")
    public void batchUpdateSelectionStatus(
            @RequestParam(required = false) Long assessmentId,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String lga,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false) DisabilityStatus disabilityStatus,
            @RequestParam(required = false) String educationLevel,
            Pageable pageable) {

        appUserService.batchUpdateSelectionStatus(
                assessmentId,
                score,
                state,
                lga,
                gender,
                minAge,
                religion,
                interest,
                disabilityStatus,
                educationLevel,
                pageable
        );
    }


}

