package com.library.model;

import java.time.LocalDate;

/**
 * Concrete User subtype representing a library Member/Student.
 * Demonstrates: Inheritance, Polymorphism, Encapsulation.
 */
public class Member extends User {

    public enum Status {
        ACTIVE, INACTIVE
    }

    private int memberId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate membershipDate;
    private Status status;

    public Member() {
        super();
        setRole(Role.MEMBER);
    }

    // Overloaded constructors - demonstrates Method/Constructor Overloading.
    public Member(String name, String email, String phone, String address) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.membershipDate = LocalDate.now();
        this.status = Status.ACTIVE;
    }

    public Member(int memberId, int userId, String username, String password, boolean active,
                  String name, String email, String phone, String address,
                  LocalDate membershipDate, Status status) {
        super(userId, username, password, Role.MEMBER, active);
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.membershipDate = membershipDate;
        this.status = status;
    }

    @Override
    public String getDashboardTitle() {
        return "Member Dashboard";
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public void setMembershipDate(LocalDate membershipDate) {
        this.membershipDate = membershipDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
