package org.saved4.saved4.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.auth.PasswordHasher;
import org.saved4.saved4.dao.RegisterDao;
import org.saved4.saved4.dto.Admin;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.UserType;
import org.saved4.saved4.exceptions.ConflictException;

import javax.crypto.SecretKey;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Singleton
public class RegisterService {
    private static final long MAGIC_LINK_DURATION = Duration.ofMinutes(15).toMillis();
    private final String base64Key = System.getenv("MAGIC_LINK_KEY");
    private final byte[] decodedKey = Base64.getDecoder().decode(base64Key);
    private final SecretKey key = Keys.hmacShaKeyFor(decodedKey);

    @Inject
    RegisterDao registerDao;

    @Context
    ContainerRequestContext requestContext;
    @Context
    UriInfo uriInfo;

    public String requestToken(User user) {
        try {
            if (requestContext.getProperty("db_role") != Role.ADMIN) {
                throw new ForbiddenException();
            }
            if (registerDao.checkIfExists((Role) requestContext.getProperty("db_role"), user.getEmail())) {
                throw new ConflictException();
            }

            if (user.getUserType() == UserType.PORT) {
                throw new InternalServerErrorException();
            }

            Date now = new Date();
            Date exp = new Date(System.currentTimeMillis() + MAGIC_LINK_DURATION);

            String token;
            if (user.getUserType() == UserType.ADMIN) {
                token = Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userType", user.getUserType())
                        .issuedAt(now)
                        .expiration(exp)
                        .signWith(key)
                        .compact();
            } else {
                Company company = (Company) user;
                token = Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userType", user.getUserType())
                        .claim("name", company.getName())
                        .claim("location", company.getLocation() != null ? company.getLocation().toString() : "0,0")
                        .claim("opening_time", company.getOpeningTime() != null ? company.getOpeningTime().toString() : "09:00:00")
                        .claim("closing_time", company.getClosingTime() != null ? company.getClosingTime().toString() : "17:00:00")
                        .issuedAt(now)
                        .expiration(exp)
                        .signWith(key)
                        .compact();
            }

            return token;
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public URI registerUser(String token, User user) {
        JwtParserBuilder jwts = Jwts.parser().requireSubject(user.getEmail());
        Claims claims = jwts.verifyWith(key).build().parseSignedClaims(token).getPayload();
        UserType userType = UserType.valueOf((String) claims.get("userType"));

        user.setPassword(PasswordHasher.hashPassword(user.getPassword()));

        if (userType == UserType.ADMIN) {
            Admin admin = (Admin) user;
            admin.setEmail(claims.getSubject());

            if (!registerAdmin(admin)) {
                throw new ConflictException();
            }
        } else {
            Company company = (Company) user;
            company.setName((String) claims.get("name"));

            // Handle PGpoint conversion from string representation
            Object locationClaim = claims.get("location");
            if (locationClaim instanceof String) {
                String locationStr = (String) locationClaim;
                try {
                    // Handle both "(x,y)" and "x,y" formats
                    if (locationStr.startsWith("(") && locationStr.endsWith(")")) {
                        company.setLocation(new PGpoint(locationStr));
                    } else {
                        // Parse "x,y" format
                        String[] parts = locationStr.split(",");
                        if (parts.length >= 2) {
                            double x = Double.parseDouble(parts[0].trim());
                            double y = Double.parseDouble(parts[1].trim());
                            company.setLocation(new PGpoint(x, y));
                        } else {
                            company.setLocation(new PGpoint(0.0, 0.0));
                        }
                    }
                } catch (Exception e) {
                    company.setLocation(new PGpoint(0.0, 0.0)); // Default location
                }
            } else {
                company.setLocation(new PGpoint(0.0, 0.0)); // Default location
            }

            // Handle Time conversion from string representation
            Object openingTimeClaim = claims.get("opening_time");
            if (openingTimeClaim instanceof String) {
                try {
                    company.setOpeningTime(Time.valueOf((String) openingTimeClaim));
                } catch (Exception e) {
                    company.setOpeningTime(Time.valueOf("09:00:00")); // Default opening time
                }
            } else {
                company.setOpeningTime(Time.valueOf("09:00:00")); // Default opening time
            }

            Object closingTimeClaim = claims.get("closing_time");
            if (closingTimeClaim instanceof String) {
                try {
                    company.setClosingTime(Time.valueOf((String) closingTimeClaim));
                } catch (Exception e) {
                    company.setClosingTime(Time.valueOf("17:00:00")); // Default closing time
                }
            } else {
                company.setClosingTime(Time.valueOf("17:00:00")); // Default closing time
            }

            if (!registerCompany(company)) {
                throw new ConflictException();
            }
        }
        return uriInfo.getAbsolutePathBuilder()
                .path(user.getUsername())
                .build();
    }

    public Object validateToken(String token) {
        try {
            JwtParserBuilder jwts = Jwts.parser();
            Claims claims = jwts.verifyWith(key).build().parseSignedClaims(token).getPayload();

            UserType userType = UserType.valueOf((String) claims.get("userType"));

            if (userType == UserType.COMPANY) {
                // Return company information from token
                return Map.of(
                        "name", claims.get("name"),
                        "email", claims.getSubject(),
                        "location", claims.get("location"),
                        "openingTime", claims.get("opening_time"),
                        "closingTime", claims.get("closing_time"),
                        "userType", userType.toString()
                );
            } else if (userType == UserType.ADMIN) {
                return Map.of(
                        "email", claims.getSubject(),
                        "userType", userType.toString()
                );
            }

            throw new InternalServerErrorException("Unsupported user type");

        } catch (Exception e) {
            throw new ForbiddenException("Invalid or expired token");
        }
    }

    private boolean registerCompany(Company company) {
        try {
            return registerDao.registerCompany(Role.SYSTEM, company).equals(company.getUsername());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    private boolean registerAdmin(Admin admin) {
        try {
            return registerDao.registerAdmin(Role.SYSTEM, admin).equals(admin.getUsername());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }
}
