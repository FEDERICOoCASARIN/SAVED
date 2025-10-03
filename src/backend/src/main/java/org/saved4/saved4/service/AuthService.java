package org.saved4.saved4.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.saved4.saved4.dao.AuthDao;
import org.saved4.saved4.dto.LoginRequest;
import org.saved4.saved4.dto.PasswordResetRequest;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.UserType;
import org.saved4.saved4.auth.PasswordHasher;

@RequestScoped
public class AuthService {
    @Inject
    AuthDao authDao;

    private static final int KEY_DURATION_MS = 3600000;
    private final String base64Key = System.getenv("JWT_KEY");
    private final byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    private final SecretKey key = Keys.hmacShaKeyFor(decodedKey);

    private final SecureRandom secureRandom = new SecureRandom();

    public Response login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        String name;
        UserType userType;
        try {
            Pair<String, UserType> pair = authDao.login(Role.SYSTEM, username, password);
            if (pair == null) {
                throw new ForbiddenException();
            }
            name = pair.getLeft();
            userType = pair.getRight();

            System.out.println("got name");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }

        //Generate a random string that will constitute the fingerprint for this user
        byte[] randomFgp = new byte[50];
        secureRandom.nextBytes(randomFgp);
        String userFingerprint = DatatypeConverter.printHexBinary(randomFgp);

        //Compute a SHA256 hash of the fingerprint in order to store the
        //fingerprint hash (instead of the raw value) in the token
        //to prevent an XSS to be able to read the fingerprint and
        //set the expected cookie itself
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return Response.serverError().build();
        }
        byte[] userFingerprintDigest =
                digest.digest(userFingerprint.getBytes(StandardCharsets.UTF_8));
        String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

        //Create the token with client context (fingerprint) information
        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + KEY_DURATION_MS);

        String token = Jwts.builder().subject(username).claim("userType", userType)
                .claim("userFingerprint", userFingerprintHash).issuedAt(now).expiration(exp)
                .signWith(key).compact();

        NewCookie tokenCookie =
                new NewCookie.Builder("token").value(token).path("/").httpOnly(true).secure(true)
                        .maxAge(7 * 24 * 60 * 60).sameSite(NewCookie.SameSite.STRICT).build();

        //Add the fingerprint in a hardened cookie
        NewCookie fingerprintNewCookie =
                new NewCookie.Builder("__Secure-Fgp").value(userFingerprint).path("/")
                        .httpOnly(true).secure(true).maxAge(7 * 24 * 60 * 60)
                        .sameSite(NewCookie.SameSite.STRICT).build();

        // user data
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
        responseBuilder.add("username", username).add("name", name).add("role", userType.toString())
                .add("exp", String.valueOf(exp.getTime() / 1000));

        return Response.ok(responseBuilder.build()) // Set the JSON object as the response entity
                .cookie(tokenCookie).header("Set-Cookie", fingerprintNewCookie).build();
    }

    public Response resetPassword(ContainerRequestContext requestContext, PasswordResetRequest request) {
        String username = (String) requestContext.getProperty("username");
        Role role = (Role) requestContext.getProperty("db_role");
        
        if (username == null || role == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            String hashedPassword = PasswordHasher.hashPassword(request.getNewPassword());
            if (authDao.updatePassword(role, username, hashedPassword)) {
                return Response.ok(Json.createObjectBuilder().add("message", "Password updated successfully").build()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder().add("message", "User not found").build())
                    .build();
            }
        } catch (SQLException e) {
            return Response.serverError()
                .entity(Json.createObjectBuilder().add("message", "Failed to update password").build())
                .build();
        }
    }
}
