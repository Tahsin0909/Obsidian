package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a fine assessed on an overdue borrowing.
 */
public class Fine implements Serializable {

    private int fineId;
    private int borrowingId;
    private int memberId;
    private double amount;
    private boolean paid;
    private LocalDate fineDate;

    public Fine() {
    }

    public Fine(int fineId, int borrowingId, int memberId, double amount, boolean paid, LocalDate fineDate) {
        this.fineId = fineId;
        this.borrowingId = borrowingId;
        this.memberId = memberId;
        this.amount = amount;
        this.paid = paid;
        this.fineDate = fineDate;
    }

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public int getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(int borrowingId) {
        this.borrowingId = borrowingId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDate getFineDate() {
        return fineDate;
    }

    public void setFineDate(LocalDate fineDate) {
        this.fineDate = fineDate;
    }
}
