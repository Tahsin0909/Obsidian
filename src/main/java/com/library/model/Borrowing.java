package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a record of a borrowed book by a library member.
 */
public class Borrowing implements Serializable {

    public enum Status {
        ACTIVE, RETURNED, OVERDUE
    }

    private int borrowingId;
    private int memberId;
    private int bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;

    public Borrowing() {
    }

    public Borrowing(int memberId, int bookId, LocalDate borrowDate, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = Status.ACTIVE;
    }

    public Borrowing(int borrowingId, int memberId, int bookId, LocalDate borrowDate,
                     LocalDate dueDate, LocalDate returnDate, Status status) {
        this.borrowingId = borrowingId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
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

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return status == Status.ACTIVE && LocalDate.now().isAfter(dueDate);
    }
}
