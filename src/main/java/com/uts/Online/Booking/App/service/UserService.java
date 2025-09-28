package com.uts.Online.Booking.App.service;

import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;

@Service 
public class UserService {
    
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User findById(Long id){
        return userDAO.findById(id).orElse(null);
    }

    public User findByEmail(String email){
        return userDAO.findByEmail(email).orElse(null);
    }


}
