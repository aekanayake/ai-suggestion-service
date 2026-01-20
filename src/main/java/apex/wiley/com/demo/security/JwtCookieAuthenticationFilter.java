package apex.wiley.com.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtCookieAuthenticationFilter.class);
    private static final String JWT_COOKIE_NAME = "X-NG-JWT-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwtToken = extractJwtFromCookie(request);

            if (jwtToken != null && !jwtToken.isEmpty()) {
                JwtClaims jwtClaims = parseJwtToken(jwtToken);

                if (jwtClaims != null) {
                    // Create authentication with JwtClaims as principal
                    String role = (String) jwtClaims.getClaimValue("lmsrole");
                    List<SimpleGrantedAuthority> authorities = role != null
                            ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            : Collections.emptyList();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(jwtClaims, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    LOG.debug("JWT authentication successful for user: {}", jwtClaims.getSubject());
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to process JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> JWT_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private JwtClaims parseJwtToken(String jwtToken) {
        try {
            // Create a JWT consumer that skips signature verification
            // In production, you should verify the signature with the public key
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setSkipSignatureVerification()
                    .setSkipDefaultAudienceValidation()
                    .setRequireSubject()
                    .build();

            return jwtConsumer.processToClaims(jwtToken);
        } catch (InvalidJwtException e) {
            LOG.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
}
