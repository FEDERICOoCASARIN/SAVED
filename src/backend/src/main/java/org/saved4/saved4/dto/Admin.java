package org.saved4.saved4.dto;

import org.saved4.saved4.enums.UserType;

public class Admin extends User {
    private String name;

    public Admin() {
    }

    public Admin(String username, String password, String email, String name) {
        super(username, password, email, UserType.ADMIN);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
