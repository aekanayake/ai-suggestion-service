package apex.wiley.com.demo.service;

import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);

    /**
     * Retrieves JWT claims from the current security context.
     *
     * @return JwtClaims from the authenticated user's token
     * @throws IllegalStateException if authentication is missing or token format is invalid
     */
    public JwtClaims getJwtClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof JwtClaims jwtClaims)) {
            LOG.error("Unexpected token format");
            throw new IllegalStateException("Invalid or missing JWT token");
        }
        return jwtClaims;
    }

    /**
     * Retrieves the LMS context ID from JWT claims.
     *
     * @return the lmscontextid claim value
     */
    public String getContextId() {
        return (String) getJwtClaims().getClaimValue("lmscontextid");
    }

    /**
     * Retrieves the LMS user ID from JWT claims.
     *
     * @return the lmsuserid claim value
     */
    public String getLtiUserId() {
        return (String) getJwtClaims().getClaimValue("lmsuserid");
    }

    /**
     * Retrieves a specific claim value from JWT claims.
     *
     * @param claimName the name of the claim to retrieve
     * @return the claim value, or null if not present
     */
    public Object getClaimValue(String claimName) {
        return getJwtClaims().getClaimValue(claimName);
    }

    /**
     * Retrieves a specific claim value as String from JWT claims.
     *
     * @param claimName the name of the claim to retrieve
     * @return the claim value as String, or null if not present
     */
    public String getClaimValueAsString(String claimName) {
        return (String) getJwtClaims().getClaimValue(claimName);
    }
}
