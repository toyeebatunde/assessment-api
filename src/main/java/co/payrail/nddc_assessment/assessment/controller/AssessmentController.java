package co.payrail.nddc_assessment.assessment.controller;

import co.payrail.nddc_assessment.assessment.dto.enums.AssessmentStatus;
import co.payrail.nddc_assessment.assessment.dto.input.UserAnswer;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.entity.Question;
import co.payrail.nddc_assessment.assessment.service.AssessmentService;
import co.payrail.nddc_assessment.assessment.service.QuestionService;
import co.payrail.nddc_assessment.config.TokenProvider;
import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    private final UserService userService;

    private final TokenProvider tokenProvider;
    private final QuestionService questionService;
    @Value("${host.url}")
    private String weburl;

    public AssessmentController(AssessmentService assessmentService, UserService userService, TokenProvider tokenProvider, QuestionService questionService) {
        this.assessmentService = assessmentService;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.questionService = questionService;
    }

    @PostMapping("/launch")
    public ResponseEntity<String> launchAssessment() {
        return ResponseEntity.ok(assessmentService.processInvites());
    }

    // Create Assessment
    @PostMapping("/create")
    public ResponseEntity<Assessment> createAssessment(@RequestBody Assessment assessment) {
        return ResponseEntity.ok(assessmentService.createAssessment(assessment));
    }

    // List All Assessments
    @GetMapping("/list")
    public ResponseEntity<List<Assessment>> listAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/fetch")
    public ResponseEntity<Page<Assessment>> listPaginatedAssessments(@RequestParam int pageNo, @PathVariable int pageSize) {
        return ResponseEntity.ok(assessmentService.getAllAssessments(pageNo,pageSize));
    }

    @GetMapping("/setup")
    public void setupAssessment(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String code,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String tokenCode = token != null ? token : code;

        try {
            // Validate the token
            userService.validateToken(tokenCode, request);

            // Fetch the user associated with the token
            AppUser user = userService.getUserById(tokenProvider.getId()).orElseThrow(
                    () -> new RuntimeException("Invalid user")
            );

            // Determine if the assessment is completed and redirect accordingly
            String url = weburl;
            if (AssessmentStatus.COMPLETED.equals(user.getAssessmentStatus())) {
                url = weburl + "/completed";
            }

            // Set JWT as a cookie and navigate to the page
            userService.navigateToPage(tokenCode, url, response);
        } catch (Exception e) {
            System.out.println("ERROR "+e);
            // In case of any exception during token validation, redirect to the completed page
            response.sendRedirect(weburl + "/expired");
        }
    }


    @PostMapping("/start")
    public ResponseEntity<List<Question>> startAssessment() {
        userService.startAssessment(tokenProvider.getId());
        return ResponseEntity.ok(questionService.getListQuestions(tokenProvider.getAssessmentId()));
    }

    // Taking an Assessment (before starting, collect user data)
    @PostMapping("/invite")
    public ResponseEntity<String> inviteToAssessment(@RequestBody AppUser user) throws UnsupportedEncodingException {
        assessmentService.recordUserDetails(user);
        return ResponseEntity.ok("User data saved, ready to start the assessment.");
    }

    @PostMapping("/submit/{id}")
    public ResponseEntity<Integer> submitAssessment(@PathVariable Long id, @RequestBody List<UserAnswer> answers) throws JsonProcessingException {
        System.out.println("GOT HERE ++++++");
        Assessment assessmentOptional = assessmentService.getAssessmentById(id);
        System.out.println("GOT HERE ++++++"+ assessmentOptional);
        Optional<AppUser> optionalAppUser = userService.findAppUserById(tokenProvider.getId());
        System.out.println("USER GOT HERE ++++++"+ optionalAppUser);
        if (optionalAppUser.isEmpty()){
            System.out.println("USER GOT HERE ++++++"+ optionalAppUser.get());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0);
        }
        AppUser user = optionalAppUser.get();
        // Check if the user has already completed the assessment
        if (user.getAssessmentStatus() == AssessmentStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0);
        }
        int score = assessmentService.gradeAssessment(assessmentOptional, answers);
        user.setScore(score);
        user.setAnswers(answers);
        user.setAssessmentStatus(AssessmentStatus.COMPLETED);
        userService.save(user);
        return ResponseEntity.ok(score);
    }
}
