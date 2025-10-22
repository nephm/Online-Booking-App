package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "userId")
@Table(name ="admins", schema ="dbo")
public class Admin extends User{

    public Admin(){ super(); }
    
    public Admin(String firstName, String lastName, String email, String password, String phoneNumber){
        super(firstName, lastName, email, password, phoneNumber);
    }
    
}
