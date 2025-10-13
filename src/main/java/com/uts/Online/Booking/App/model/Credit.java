package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Credit")
public class Credit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditId;

    @Column(name="user_id")
    private Long userId;

    @Column(name="payment_id")
    private Long paymentId;

    @Column(name="booking_id")
    private Long bookingId;

    @Column(name="amount")
    private double amount; 

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getCreditId() {
        return creditId;
    }

    public void setCreditId(Long creditId) {
        this.creditId = creditId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
