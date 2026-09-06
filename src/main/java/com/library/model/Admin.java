package com.library.model;

/**
 * Concrete User subtype representing a Librarian/Admin.
 * Demonstrates: Inheritance, Polymorphism (overrides getDashboardTitle), Constructors.
 */
public class Admin extends User {

    private String fullName;
    private String email;

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int userId, String username, String password, boolean active, String fullName, String email) {
        super(userId, username, password, Role.ADMIN, active);
        this.fullName = fullName;
        this.email = email;
    }

    @Override
    public String getDashboardTitle() {
        return "Administrator Dashboard";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
