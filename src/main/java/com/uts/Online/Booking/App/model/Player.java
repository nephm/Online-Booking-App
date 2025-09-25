package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
@Table(name ="players", schema ="dbo")
public class Player extends User {

    @Column(name="payment_id")
    private int paymentId;

    @Column(name="address")
    private String address;

    @Column(name="creditBalance", nullable = false)
    private double creditBalance = 0.0;
    

    public Player(){ super(); }

    //new customer
    public Player(String firstName, String lastName, String email, String password, String phoneNumber, String address) {
        super( firstName, lastName, email, password, phoneNumber);
        this.address = address;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(double creditBalance) {
        this.creditBalance = creditBalance;
    }
}
