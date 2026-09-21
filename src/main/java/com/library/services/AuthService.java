package com.library.services;

import com.library.model.Admin;
import com.library.model.Member;
import com.library.model.User;
import com.library.store.DataStore;
import com.library.util.PasswordUtil;

/**
 * Handles login for both roles behind a single LoginFrame.
 * Demonstrates: Polymorphism (returns a User reference that is really an Admin or Member at runtime).
 * Looks up credentials directly against the in-memory DataStore — no external database.
 */
public class AuthService {

    private final DataStore store = DataStore.getInstance();

    /**
     * Authenticates an email or username and password pair and returns the fully-populated User
     * (an Admin or a Member instance), or null if the credentials are invalid or the account is inactive.
     */
    public User login(String emailOrUsername, String password) {
        if (emailOrUsername == null || password == null) {
            return null;
        }
        String trimmed = emailOrUsername.trim();
        if (trimmed.isEmpty() || password.isEmpty()) {
            return null;
        }

        // 1. Check Admin accounts (supports logging in via "admin" or "admin@mail.com")
        Admin admin = store.findAdminByUsernameOrEmail(trimmed);
        if (admin != null) {
            if (admin.isActive() && PasswordUtil.verify(password, admin.getPassword())) {
                return admin;
            }
            return null;
        }

        // 2. Check Member accounts (supports logging in via "member" or "member@mail.com")
        Member member = store.findMemberByUsernameOrEmail(trimmed);
        if (member != null) {
            if (member.isActive() && member.getStatus() == Member.Status.ACTIVE
                    && PasswordUtil.verify(password, member.getPassword())) {
                return member;
            }
            return null;
        }

        return null;
    }
}
