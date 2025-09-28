package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name ="users", schema ="dbo")
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="userID")
    private Long id;

    @Column(name="user_fname")
    private String firstName;

     @Column(name="user_lname")
    private String lastName;

    @Column(name="user_email", nullable = false, unique = true)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="user_phone")
    private String phoneNumber;

    @Column(name="is_active")
    private boolean isActive = false;

    @Column(name="activation_token")
    private String activationToken;

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String email, String password, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setActivationToken(String verificationToken) {
        this.activationToken = verificationToken;
    }
}
