package com.library.model;

import java.io.Serializable;

/**
 * Abstract base class representing any authenticated user of the system.
 * Demonstrates: Abstraction, Encapsulation, Inheritance (base for Admin/Member).
 */
public abstract class User implements Serializable {

    public enum Role {
        ADMIN, MEMBER
    }

    private int userId;
    private String username;
    private String password; // stored as SHA-256 hash
    private Role role;
    private boolean active;

    public User() {
    }

    public User(int userId, String username, String password, Role role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    // Abstract method - forces subclasses to define their own dashboard label.
    // Demonstrates: Abstraction / Polymorphism.
    public abstract String getDashboardTitle();

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role=" + role + "}";
    }
}
