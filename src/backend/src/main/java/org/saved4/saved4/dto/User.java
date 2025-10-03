package org.saved4.saved4.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.saved4.saved4.enums.UserType;

import java.io.Serializable;
import java.sql.Timestamp;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "userType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = Company.class, name = "COMPANY")
})
public class User implements Serializable {
    private String username;
    private String password;
    private String email;
    private Timestamp created_at;
    private Timestamp modified_at;
    private UserType userType;

    public User() {
    }

    public User(String username, String password, String email, Timestamp created_at, Timestamp modified_at, UserType userType) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.created_at = created_at;
        this.modified_at = modified_at;
        this.userType = userType;
    }

    public User(String username, String password, String email, UserType userType) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getModified_at() {
        return modified_at;
    }

    public void setModified_at(Timestamp modified_at) {
        this.modified_at = modified_at;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
