package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
public class Admin extends User{
    
    public Admin(Long id, String firstName, String lastName, String email, String password, int phoneNumber){
        super(id, firstName, lastName, email, password, phoneNumber);
    }
    
}
