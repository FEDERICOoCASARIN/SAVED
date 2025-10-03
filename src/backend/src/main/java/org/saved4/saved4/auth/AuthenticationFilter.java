package org.saved4.saved4.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.saved4.saved4.dao.UserDao;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.UserType;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private final String base64Key = System.getenv("JWT_KEY");
    private final byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    private final SecretKey key = Keys.hmacShaKeyFor(decodedKey);

    @Inject
    private UserDao userDao;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (isExcluded(path)) {
            return;
        }

        Cookie tokenCookie = requestContext.getCookies().get("token");
        Cookie fgpCookie = requestContext.getCookies().get("__Secure-Fgp");


        if (tokenCookie == null || tokenCookie.getValue().isEmpty() || fgpCookie == null ||
                fgpCookie.getValue().isEmpty()) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = tokenCookie.getValue();

        try {
            String userFingerprint = fgpCookie.getValue();
            //Compute a SHA256 hash of the received fingerprint in cookie in order to compare
            //it to the fingerprint hash stored in the token
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] userFingerprintDigest =
                    digest.digest(userFingerprint.getBytes(StandardCharsets.UTF_8));
            String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);
            //Verify the token, if the verification fail then an exception is thrown
            JwtParserBuilder jwts = Jwts.parser().require("userFingerprint", userFingerprintHash);
            Claims claims = jwts.verifyWith(key).build().parseSignedClaims(token).getPayload();
            //Token verified
            String username = claims.getSubject();
            UserType userType = UserType.valueOf((String) claims.get("userType"));

            // Check if user actually exists in database and is active
            try {
                Role dbRole = getRole(userType);
                User user = userDao.getUserByUsername(dbRole, username);
                if (user == null) {
                    abortWithUnauthorized(requestContext);
                    return;
                }
                if (user.getUserType() != userType) {

                    abortWithUnauthorized(requestContext);
                    return;
                }
            } catch (SQLException e) {
                System.err.println(
                        "Database error while validating user " + username + ": " + e.getMessage());
                abortWithUnauthorized(requestContext);
                return;
            }

            requestContext.setProperty("username", username);
            requestContext.setProperty("userType", userType);
            requestContext.setProperty("db_role", getRole(userType));

        } catch (JwtException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            abortWithUnauthorized(requestContext);
        }
    }

    public static boolean isExcluded(String path) {
        return path.equals("auth/login") ||
                (path.startsWith("register") && !path.equals("register"));

    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                                         .entity("User cannot access the resource.").build());
    }

    private Role getRole(UserType userType) {
        switch (userType) {
            case UserType.ADMIN -> {
                return Role.ADMIN;
            }
            case UserType.COMPANY -> {
                return Role.COMPANY;
            }
            default -> {
                return null;
            }
        }
    }
}
