package co.payrail.nddc_assessment.assessment.service;

import co.payrail.nddc_assessment.applicant.entity.Applicant;
import co.payrail.nddc_assessment.applicant.service.ApplicantService;
import co.payrail.nddc_assessment.assessment.dto.enums.AssessmentStatus;
import co.payrail.nddc_assessment.assessment.dto.input.UserAnswer;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.entity.Option;
import co.payrail.nddc_assessment.assessment.entity.Question;
import co.payrail.nddc_assessment.assessment.repository.AssessmentRepository;
import co.payrail.nddc_assessment.config.ThreadConfig;
import co.payrail.nddc_assessment.config.TokenProvider;
import co.payrail.nddc_assessment.integration.service.MailService;
import co.payrail.nddc_assessment.integration.termii.model.Email;
import co.payrail.nddc_assessment.integration.termii.service.Termii;
import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.repository.UserRepository;
import co.payrail.nddc_assessment.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AssessmentService {


    private AssessmentRepository assessmentRepository;

    private final QuestionService questionService;


    private UserRepository userRepository;

    private final UserService userService;

    private final ApplicantService applicantService;
    private final ThreadPoolTaskExecutor executorService = ThreadConfig.getExecutor();

    private final TokenProvider tokenProvider;

    private final Termii termiiService;

    private final MailService mailService;

    @Autowired
    public AssessmentService(AssessmentRepository assessmentRepository, QuestionService questionService, UserRepository userRepository, UserService userService, ApplicantService applicantService, TokenProvider tokenProvider, Termii termiiService, MailService mailService) {
        this.assessmentRepository = assessmentRepository;
        this.questionService = questionService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.applicantService = applicantService;
        this.tokenProvider = tokenProvider;
        this.termiiService = termiiService;
        this.mailService = mailService;
    }

    public Assessment createAssessment(Assessment assessment) {
        return assessmentRepository.save(assessment);
    }

    public List<Assessment> getAllAssessments() {
        return assessmentRepository.findAll();
    }

    public Page<Assessment> getAllAssessments(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo,pageSize);
        return assessmentRepository.findAll(pageable);
    }

    public void recordUserDetails(AppUser user) throws UnsupportedEncodingException {
        // Check if a user with the same userName and assessmentId already exists
        Optional<AppUser> existingUser = userRepository.findByUserNameAndAssessmentId(user.getUserName(), user.getAssessmentId());

        if (existingUser.isPresent()) {
            // Handle response back if the user with the assessmentId has been registered before
            System.out.println("User with userName: " + user.getUserName() + " and assessmentId: " + user.getAssessmentId() + " is already registered.");
            return; // Exit early to avoid re-registering the user
        }

        // Proceed with saving the user and sending the invite
        AppUser savedUser = userRepository.save(user);
        String inviteUrl = generateInviteUrl(savedUser);
        System.out.println("Generated inviteUrl ++++ " + inviteUrl);

        if (!savedUser.getPhoneNumber().isEmpty()) {
            sendInviteViaPhone(savedUser.getPhoneNumber(), inviteUrl);
        }

        if (!savedUser.getEmail().isEmpty()) {
            sendInviteViaEmail(savedUser.getEmail(), inviteUrl);
        }
    }


    public String generateInviteUrl(AppUser user) throws UnsupportedEncodingException {
        String baseUrl= "https://assessment-api.nddc.gov.ng/api/assessments/setup";

        String token = tokenProvider.generateAssessmentJWTToken(user);

        return baseUrl + "?token=" + token;
    }

    private void sendInviteViaPhone(String phoneNumber, String invite) {
        executorService.submit(() -> {
            try {
                termiiService.sendSmsTo(phoneNumber, invite);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not send sms");
            }
        });
    }

    private void sendInviteViaEmail(String emailAddress, String invite) {
        executorService.submit(() -> {
        try {
            Context context = new Context();
            context.setVariable("code", invite);

            Email email = new Email.Builder()
                    .setRecipient(emailAddress)
                    .setSubject("Your Assessment Invite")
                    .setTemplate("mail/assessment_invite")
                    .build();

            mailService.sendMailgunMail(email, context);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("could not send otp via email");
        }
        });
    }

    public int gradeAssessment(Assessment assessment, List<UserAnswer> userAnswers) {
        int score = 0;

        // Retrieve the questions for the given assessment
        List<Question> questions = questionService.getListQuestions(assessment.getId());

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            UserAnswer answer = userAnswers.get(i);

            switch (question.getQuestionType()) {
                case MCQ:
                    // Check selected options for MCQ (multiple-choice)
                    List<Option> correctOptions = question.getOptions().stream()
                            .filter(Option::isCorrect)
                            .collect(Collectors.toList());
                    // Assuming answer.getSelectedOptions() returns the selected options for the question
                    if (correctOptions.equals(answer.getSelectedOptions())) {
                        score += question.getAwardedScore(); // Add points for a correct answer
                    }
                    break;

                case TRUE_FALSE:
                    // Check True/False question
                    Option selectedOption = answer.getSelectedOptions().get(0); // Only one option is selected
                    if (selectedOption.isCorrect()) {
                        score += question.getAwardedScore();
                    }
                    break;

                case SHORT_ANSWER:
                    // Check Short Answer question
                    if (question.getCorrectAnswer().equalsIgnoreCase(answer.getShortAnswer())) {
                        score += question.getAwardedScore();
                    }
                    break;

                case MULTI_CORRECT:
                    // Handle multiple correct answers, similar logic as MCQ
                    List<Option> selectedOptions = answer.getSelectedOptions();
                    int correctSelections = 0;
                    for (Option selected : selectedOptions) {
                        if (selected.isCorrect()) correctSelections++;
                    }
                    score += (correctSelections * question.getAwardedScore()); // Award points for each correct answer selected
                    break;
            }
        }

        return score;
    }


    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    // Fetch users based on the assessment they are taking
    public List<AppUser> getUsersByAssessment(Long assessmentId) {
        return userRepository.findByAssessmentId(assessmentId);
    }

    public String processInvites() {
        int pageNumber = 0;
        List<Applicant> applicants;

        do {
            // Fetch applicants in batches
            applicants = applicantService.fetchApplicants(pageNumber);

            // Convert to AppUser and filter eligible users, passing assessmentId
            List<AppUser> eligibleUsers = applicants.stream()
                    .map(applicant -> convertToAppUser(applicant))  // Pass both applicant and assessmentId
                    .collect(Collectors.toList());

            // Batch save eligible users
            userService.saveUsersInBatch(eligibleUsers);

            pageNumber++;

        } while (!applicants.isEmpty());  // Keep processing until all applicants are processed

        return "done";
    }


    private boolean isEligible(Applicant applicant) {
        // Define eligibility criteria here
        return calculateAge(applicant.getDateOfBirth()) >= 35;  // Example eligibility check
    }

    private AppUser convertToAppUser(Applicant applicant) {
        AppUser user = new AppUser();
        String userName = !applicant.getPhoneNumber().isEmpty()?applicant.getPhoneNumber():applicant.getEmail();
        Long assessmentId = Long.valueOf(applicant.getEducationLevel() == "Secondary"?2:1);
        user.setUserName(userName);
        user.setName(applicant.getFirstName()+ " "+ applicant.getLastName());
        user.setPhoneNumber(applicant.getPhoneNumber());
        user.setAge(calculateAge(applicant.getDateOfBirth()));
        user.setGender(applicant.getGender());
        user.setEducationLevel(applicant.getEducationLevel());
        user.setInternCode(applicant.getInternCode());
        user.setState(applicant.getState());
        user.setStateOfOrigin(applicant.getStateOfOrigin());
        user.setOriginLGA(applicant.getOriginLGA());
        user.setAssessmentStatus(AssessmentStatus.PENDING);
        user.setAssessmentId(assessmentId);
        // Set other user details
//        recordUserDetails(user);
        return user;
    }

    public int calculateAge(String dobString) {
        // Define the formatter to parse the date string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

        // Convert the date string to a LocalDate object
        LocalDate dob = LocalDate.parse(dobString, formatter);

        // Get the current date
        LocalDate today = LocalDate.now();

        // Calculate the age using the Period class
        return Period.between(dob, today).getYears();
    }


    // You will need methods to process the grading and handle responses
}

