// package com.uts.Online.Booking.App.model;

// import jakarta.persistence.*;

// @Entity
// @Table(name ="users")
// public class User {
    
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String firstName;
//     private String lastName;

//     @Column(nullable = false, unique = true)
//     private String email;

//     @Column(nullable = false)
//     private String password;
//     private int phoneNumber;

//     // Constructors
//     public User() {}

//     public User(Long id, String firstName, String lastName, String email, String password, int phoneNumber) {
//         this.id = id;
//         this.firstName = firstName;
//         this.lastName = lastName;
//         this.email = email;
//         this.password = password;
//         this.phoneNumber = phoneNumber;
//     }

//     // Getters and Setters
//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getFirstName() {
//         return firstName;
//     }

//     public void setFirstName(String firstName) {
//         this.firstName = firstName;
//     }

//     public String getlastName() {
//         return lastName;
//     }

//     public void setlastName(String lastName) {
//         this.lastName = lastName;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public int getPhoneNumber() {
//         return phoneNumber;
//     }

//     public void setPhoneNumber(int phoneNumber) {
//         this.phoneNumber = phoneNumber;
//     }
// }
