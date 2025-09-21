package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
public class Player extends User {

    private Long paymentId;
    private String address;
    
    public Player(Long id, String firstName, String lastName, String email, String password, String phoneNumber, Long paymentId, String address) {
        super(id, firstName, lastName, email, password, phoneNumber);
        this.paymentId = paymentId;
        this.address = address;
    }

    //new customer
    public Player(Long id, String firstName, String lastName, String email, String password, String phoneNumber, String address) {
        super(id, firstName, lastName, email, password, phoneNumber);
        this.address = address;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    
}
