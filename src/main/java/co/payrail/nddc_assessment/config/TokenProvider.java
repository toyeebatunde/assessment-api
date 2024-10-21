package co.payrail.nddc_assessment.config;


import co.payrail.nddc_assessment.assessment.dto.enums.AssessmentStatus;
import co.payrail.nddc_assessment.users.entity.AppUser;
import co.payrail.nddc_assessment.users.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.payrail.nddc_assessment.users.dto.input.Constants.AUTHORITIES_KEY;

@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class TokenProvider {
    private final UserRepository userRepository;

    @Value("${jjwt.secret.key}")
    private String secret;

    private Key key;

    private Long id;

    private Long assessmentId;

    private String username;

    private String token;

    private AssessmentStatus assessmentStatus;
    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void setDetails(String token) {

        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(cleanupReceivedToken(token));

        Claims claims = claimsJws.getBody();

        // Extract and set values, with null checks to avoid NullPointerException
        if (claims.get("userId") != null) {
           int  intValue = (int) claims.get("userId");
            String intToString = Integer.toString(intValue);
            this.id = Long.valueOf(intToString);
        }

        if (claims.get("assessmentId") != null) {
            int  intValue = (int) claims.get("assessmentId");
            String intToString = Integer.toString(intValue);
            this.assessmentId = Long.valueOf(intToString);
        }

        this.username = claims.get("username") != null ? claims.get("username").toString() : null;
        this.assessmentStatus = claims.get("assessmentStatus") != null ? AssessmentStatus.valueOf(claims.get("assessmentStatus").toString()) : null;
//        this.permissions = claims.get("permissions") != null ? (List<String>) claims.get("permissions") : null;
        this.token = cleanupReceivedToken(token);

        // Optionally log or handle cases where required fields are null
        if (this.id == null || this.username == null) {
            System.err.println("Missing required claims in the token");
        }


    }

    // Method to get username from the "username" claim in the JWT
    public String getUsernameFromJWTToken(String token) throws UnsupportedEncodingException {
        String decodedString = URLDecoder.decode(cleanupReceivedToken(token), StandardCharsets.UTF_8.toString());
        return getClaimFromJWTToken(decodedString, claims -> claims.get("username", String.class));
    }

    public Integer getUserIdFromJWTToken(String token) throws UnsupportedEncodingException {
        String decodedString = URLDecoder.decode(cleanupReceivedToken(token), StandardCharsets.UTF_8.toString());
        return getClaimFromJWTToken(decodedString, claims -> claims.get("userId", Integer.class));
    }

    public Date getExpirationDateFromJWTToken(String token) {
        return getClaimFromJWTToken(cleanupReceivedToken(token), Claims::getExpiration);
    }

    public <T> T getClaimFromJWTToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromJWTToken(cleanupReceivedToken(token));
        // To inspect claims
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromJWTToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // Assuming secretKey is correctly initialized
                .build()
                .parseClaimsJws(cleanupReceivedToken(token))
                .getBody();
    }

    public Boolean isJWTTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromJWTToken(cleanupReceivedToken(token));
        return expirationDate.before(new Date());
    }

    public String generateJWTToken(AppUser user) {


        return Jwts.builder()
                .setSubject(user.getUserName()) // Sets the subject
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .claim("username", user.getUserName())  // Add "username" claim
                .claim("userId", user.getId())  // Add "userId" claim
                .claim("assessmentId", user.getAssessmentId())
                .claim("assessmentStatus", user.getAssessmentStatus())
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Issue timestamp
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAssessmentJWTToken(AppUser user) throws UnsupportedEncodingException {
        String token = Jwts.builder()
                .setSubject(user.getUserName())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .claim("username", user.getUserName())
                .claim("userId", user.getId())
                .claim("assessmentId", user.getAssessmentId())
                .claim("assessmentStatus", user.getAssessmentStatus())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // Ensure the token is URL-safe by encoding it
        return URLEncoder.encode(prepareTokenForSMS(token), StandardCharsets.UTF_8.toString());
    }

    public String prepareTokenForSMS(String token) {
        // Replace underscores with hyphens before sending
        return token.replace("_", "~");
    }

    public String cleanupReceivedToken(String token) {
        // Replace hyphens back to underscores
        return token.replace("~", "_");
    }


    public String generateTokenForVerification(String id) {
        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 25200000L)) // 7 hours
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateJWTToken(String token, UserDetails userDetails) throws UnsupportedEncodingException {
        final String username = getUsernameFromJWTToken(cleanupReceivedToken(token));
        return (username.equals(userDetails.getUsername()) && !isJWTTokenExpired(cleanupReceivedToken(token)));
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(String token, Authentication existingAuth, UserDetails userDetails) {
        Claims claims = getAllClaimsFromJWTToken(cleanupReceivedToken(token));
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
