package co.payrail.nddc_assessment.users.service.implementation;


import co.payrail.nddc_assessment.assessment.dto.enums.AssessmentStatus;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.repository.AssessmentRepository;
import co.payrail.nddc_assessment.config.ThreadConfig;
import co.payrail.nddc_assessment.config.TokenProvider;
import co.payrail.nddc_assessment.users.dto.enums.DisabilityStatus;
import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService implements co.payrail.nddc_assessment.users.service.UserService {

    private final UserRepository userRepository;

    private final AssessmentRepository assessmentRepository;
    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private ModelMapper mapper;

    private final MessageSource messageSource;

    private final Locale locale = LocaleContextHolder.getLocale();

    private final ThreadPoolTaskExecutor executorService = ThreadConfig.getExecutor();



    @Value("${host.url}")
    private String hostUrl;


    @Autowired
    public UserService(UserRepository userRepository, AssessmentRepository assessmentRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder, ModelMapper mapper, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;

        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return new org.springframework.security.core.userdetails.User(username, "", getAuthority(user));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + userId));
        return new org.springframework.security.core.userdetails.User(user.getUserName(), "", getAuthority(user));
    }

    private Collection<SimpleGrantedAuthority> getAuthority(AppUser user) {
        // If the user has no role, return a default authority, e.g., "Basic"
        return List.of(new SimpleGrantedAuthority("ROLE_BASIC"));
    }

    @Override
    public Optional<AppUser> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public Optional<AppUser> findAppUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public AppUser createUserForAssessment(Long assessmentId, AppUser appUser) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        appUser.setAssessmentId(assessment.getId());  // Tie user to assessment
        appUser.setAssessmentStatus(AssessmentStatus.PENDING);
        return userRepository.save(appUser);
    }

    @Override
    public AppUser createBatchUserForAssessment(Long assessmentId, List<AppUser> appUser) {

        return null;
    }
    @Override
    @Transactional
    public void saveUsersInBatch(List<AppUser> users) {
        userRepository.saveAll(users);  // Batch save users
    }

    @Override
    public void startAssessment(Long userId) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + userId));

        if (user.getAssessmentStatus() == AssessmentStatus.COMPLETED) {
            throw new RuntimeException("Assessment already completed");
        }

        user.setAssessmentStatus(AssessmentStatus.STARTED);
        userRepository.save(user);

    }

    @Override
    public void save(AppUser user) {
        userRepository.save(user);
    }


    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.startsWith("+234") && (phoneNumber.length() == 14);
    }

    private boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)){
            return false;
        }
        String regex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public List<AppUser> filterAppUsers(Long assessmentId, Integer score, String state, String lga, String gender, Integer minAge,
                                        String religion,
                                        String interest,
                                        DisabilityStatus disabilityStatus,
                                        Boolean selectionStatus,
                                        String educationLevel) {
        Specification<AppUser> spec = Specification.where(null);

        if (assessmentId != null) {
            spec = spec.and(AppUserSpecification.hasAssessmentId(assessmentId));
        }

        if (score != null) {
            spec = spec.and(AppUserSpecification.hasScoreGreaterThan(score));
        }

        if (state != null) {
            spec = spec.and(AppUserSpecification.hasState(state));
        }

        if (lga != null) {
            spec = spec.and(AppUserSpecification.hasAddressLGA(lga));
        }

        if (gender != null) {
            spec = spec.and(AppUserSpecification.hasGender(gender));
        }

        if (minAge != null) {
            spec = spec.and(AppUserSpecification.hasAgeGreaterThanOrEqual(minAge));
        }

        if (religion != null) {
            spec = spec.and(AppUserSpecification.hasReligion(religion));
        }

        if (interest != null) {
            spec = spec.and(AppUserSpecification.hasInterest(interest));
        }

        if (disabilityStatus != null) {
            spec = spec.and(AppUserSpecification.hasDisabilityStatus(disabilityStatus));
        }

        if (educationLevel != null) {
            spec = spec.and(AppUserSpecification.hasEducationLevel(educationLevel));
        }

        if (selectionStatus != null) {
            spec = spec.and(AppUserSpecification.hasSelectionStatus(selectionStatus));
        }

        return userRepository.findAll(spec);
    }

    @Override
    public Page<AppUser> filterAppUsers(
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
            Pageable pageable) {

        Specification<AppUser> spec = Specification.where(null);

        if (assessmentId != null) {
            spec = spec.and(AppUserSpecification.hasAssessmentId(assessmentId));
        }

        if (score != null) {
            spec = spec.and(AppUserSpecification.hasScoreGreaterThan(score));
        }

        if (state != null) {
            spec = spec.and(AppUserSpecification.hasState(state));
        }

        if (lga != null) {
            spec = spec.and(AppUserSpecification.hasAddressLGA(lga));
        }

        if (gender != null) {
            spec = spec.and(AppUserSpecification.hasGender(gender));
        }

        if (minAge != null) {
            spec = spec.and(AppUserSpecification.hasAgeGreaterThanOrEqual(minAge));
        }

        if (religion != null) {
            spec = spec.and(AppUserSpecification.hasReligion(religion));
        }

        if (interest != null) {
            spec = spec.and(AppUserSpecification.hasInterest(interest));
        }

        if (disabilityStatus != null) {
            spec = spec.and(AppUserSpecification.hasDisabilityStatus(disabilityStatus));
        }

        if (educationLevel != null) {
            spec = spec.and(AppUserSpecification.hasEducationLevel(educationLevel));
        }

        if (educationLevel != null) {
            spec = spec.and(AppUserSpecification.hasEducationLevel(educationLevel));
        }

        if (selectionStatus != null) {
            spec = spec.and(AppUserSpecification.hasSelectionStatus(selectionStatus));
        }

        return userRepository.findAll(spec, pageable);
    }
    @Override
    @Transactional
    public void batchUpdateSelectionStatus(
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
            Pageable pageable) {

        // Get the filtered AppUsers using the existing filterAppUsers method
        Page<AppUser> filteredUsers = filterAppUsers(
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
                false,
                pageable
        );

        // Extract the list of users
        List<AppUser> usersToUpdate = filteredUsers.getContent();

        // Set selectionStatus to true for each filtered user
        usersToUpdate.forEach(user -> user.setSelectionStatus(true));

        // Batch save the updated users
        userRepository.saveAll(usersToUpdate);
    }

    @Override
    public AppUser updateUser(AppUser userDTO) {
      return  userRepository.save(userDTO);
    }

    // Retrieve user by ID
    @Override
    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Update user details with partial updates
    @Override
    public AppUser updateUser(Long id, AppUser updatedUser) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (updatedUser.getUserName() != null) {
                        existingUser.setUserName(updatedUser.getUserName());
                    }
                    if (updatedUser.getName() != null) {
                        existingUser.setName(updatedUser.getName());
                    }
                    if (updatedUser.getAge() != 0) {
                        existingUser.setAge(updatedUser.getAge());
                    }
                    if (updatedUser.getLocation() != null) {
                        existingUser.setLocation(updatedUser.getLocation());
                    }
                    if (updatedUser.getGender() != null) {
                        existingUser.setGender(updatedUser.getGender());
                    }
                    if (updatedUser.getReligion() != null) {
                        existingUser.setReligion(updatedUser.getReligion());
                    }

                    if (updatedUser.getInterest() != null) {
                        existingUser.setInterest(updatedUser.getInterest());
                    }

                    if (!Objects.isNull(updatedUser.getDisabilityStatus())) {
                        existingUser.setDisabilityStatus(updatedUser.getDisabilityStatus());
                    }
                    if (updatedUser.getEducationLevel() != null) {
                        existingUser.setEducationLevel(updatedUser.getEducationLevel());
                    }
                    if (updatedUser.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                    }
                    if (updatedUser.getEmail() != null) {
                        existingUser.setEmail(updatedUser.getEmail());
                    }
                    if (updatedUser.getInternCode() != null) {
                        existingUser.setInternCode(updatedUser.getInternCode());
                    }
                    if (updatedUser.getState() != null) {
                        existingUser.setState(updatedUser.getState());
                    }
                    if (updatedUser.getStateOfOrigin() != null) {
                        existingUser.setStateOfOrigin(updatedUser.getStateOfOrigin());
                    }
                    if (updatedUser.getOriginLGA() != null) {
                        existingUser.setOriginLGA(updatedUser.getOriginLGA());
                    }

                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @Override
    public void validateToken(String token, HttpServletRequest request) throws UnsupportedEncodingException {

        String username;
        try {
            String cleanToken = cleanupReceivedToken(token);
            username = tokenProvider.getUsernameFromJWTToken(cleanToken);

            tokenProvider.setDetails(cleanToken);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(e.getMessage());
        } catch (ExpiredJwtException e) {

            throw new JwtException(e.getMessage());
        } catch (SignatureException e) {

            throw new JwtException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = loadUserByUsername(username);

            if (tokenProvider.validateJWTToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                tokenProvider.setDetails(token);
            }
        }
    }

    public String prepareTokenForSMS(String token) {
        // Replace underscores with hyphens before sending
        return token.replace("_", "~");
    }

    public String cleanupReceivedToken(String token) {
        // Replace hyphens back to underscores
        return token.replace("~", "_");
    }


    @Override
    public void navigateToPage(String token, String url, HttpServletResponse response) throws IOException {
        // Create a JWT cookie
        Cookie jwtCookie = new Cookie("nddcjwt", token);
        jwtCookie.setHttpOnly(true);  // Prevent JavaScript access to cookie
        jwtCookie.setSecure(true);    // Ensure cookie is only sent over HTTPS
        jwtCookie.setPath("/");       // Cookie available to all pages within the domain
        jwtCookie.setMaxAge(60 * 60); // Cookie expiry set to 1 hour (3600 seconds)

//        // SameSite policy to control cross-origin cookie behavior (Optional but recommended)
//        // Add this if your server supports SameSite cookies
//        jwtCookie.setSameSite("Strict");  // Options: "Strict", "Lax", or "None"

        // Add domain if necessary, especially for subdomains
        // Uncomment this line and replace "yourdomain.com" with your actual domain
         jwtCookie.setDomain("icy-rock-016506403.5.azurestaticapps.net");

        // Add cookie to the response
        response.addCookie(jwtCookie);

        // Redirect to the target page
        response.sendRedirect(url+"?token="+token);
    }


}
