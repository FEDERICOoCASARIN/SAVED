package org.saved4.saved4.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.PGpointDeserializer;
import org.saved4.saved4.enums.UserType;

public class Company extends User implements Serializable {
    private String name;
    @JsonDeserialize(using = PGpointDeserializer.class)
    private PGpoint location;
    private Time openingTime;
    private Time closingTime;


    public Company() {
    }

    public Company(String name, PGpoint location, Time openingTime, Time closingTime) {
        this.name = name;
        this.location = location;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Company(String username, String password, String email, Timestamp created_at,
                   Timestamp modified_at, UserType userType, String name, PGpoint location,
                   Time openingTime, Time closingTime) {
        super(username, password, email, created_at, modified_at, userType);
        this.name = name;
        this.location = location;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Company(String username, String password, String email, UserType userType, String name,
                   PGpoint location, Time openingTime, Time closingTime) {
        super(username, password, email, userType);
        this.name = name;
        this.location = location;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Company(String username, String name, PGpoint location, Time openingTime,
                   Time closingTime, String email) {
        setUsername(username);
        this.name = name;
        this.location = location;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        setEmail(email);
        setUserType(UserType.COMPANY);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PGpoint getLocation() {
        return location;
    }

    public void setLocation(PGpoint location) {
        this.location = location;
    }

    public Time getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Time openingTime) {
        this.openingTime = openingTime;
    }

    public Time getClosingTime() {
        return closingTime;
    }

    @Override
    public String toString() {
        return "Company{" + "name='" + name + '\'' + ", location=" + location + ", openingTime=" +
                openingTime + ", closingTime=" + closingTime + '}';
    }

    public void setClosingTime(Time closingTime) {
        this.closingTime = closingTime;
    }
}
