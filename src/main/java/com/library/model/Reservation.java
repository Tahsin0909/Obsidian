package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a book reservation placed by a member.
 */
public class Reservation implements Serializable {

    public enum Status {
        PENDING, FULFILLED, CANCELLED
    }

    private int reservationId;
    private int memberId;
    private int bookId;
    private LocalDate reservationDate;
    private Status status;

    public Reservation() {
    }

    public Reservation(int reservationId, int memberId, int bookId, LocalDate reservationDate, Status status) {
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
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

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
