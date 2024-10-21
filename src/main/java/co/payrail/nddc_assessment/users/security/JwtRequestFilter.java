package co.payrail.nddc_assessment.users.security;

import co.payrail.nddc_assessment.config.TokenProvider;
import co.payrail.nddc_assessment.users.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Objects;

public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final TokenProvider jwtUtil;

    @Autowired
    public JwtRequestFilter(TokenProvider jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String username = null;
        Long userId = null;

        // First, check if the Authorization header contains the JWT token
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else {
            // If Authorization header is not present, check if the JWT is in cookies
            Cookie[] cookies = request.getCookies();
            System.out.println("COOKIES___ " + cookies);
            if (cookies != null) {
                System.out.println("COOKIES__NOT NULL_ ");
                for (Cookie cookie : cookies) {
                    System.out.println("COOKIE___ " + cookie);
                    if (cookie.getName().equals("nddcjwt")) {

                        jwt = cookie.getValue();
                        System.out.println("JWT__ " + jwt);
                        break;
                    }
                }
            }
        }

        if (jwt != null) {
            try {
                System.out.println("JWT__2 " + jwt);
               Integer userIdInt = jwtUtil.getUserIdFromJWTToken(jwt);
                System.out.println("JWT_USERID_3 " + userIdInt);
                if (userIdInt != null) {
                    System.out.println("JWT_USERID_4 " + userIdInt);
                    String intToString = Integer.toString(userIdInt);
                    System.out.println("JWT_USERID_5 " + intToString);
                    userId = Long.valueOf(intToString);

                    System.out.println("JWT_USERID_6 " + userId);
                }
            } catch (Exception e) {
                logger.error("An error occurred while fetching username from token", e);
            }
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUserId(userId);
            System.out.println("JWT_USERID_7 " + userId);
            if (jwtUtil.validateJWTToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                setUserDetailsInTokenProvider(jwt);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setUserDetailsInTokenProvider(String authToken) {
        jwtUtil.setDetails(authToken);
    }
}
