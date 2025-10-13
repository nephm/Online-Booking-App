package com.uts.Online.Booking.App.service;

import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;

import java.util.List;

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

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public void updateUserRole(Long userId, String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_USER".equals(role)) {
            throw new IllegalArgumentException("Invalid role");
        }

        User userToUpdate = userDAO.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        userToUpdate.setRole(role);
        userDAO.save(userToUpdate);
    }
}
