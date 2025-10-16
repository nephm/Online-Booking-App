package com.uts.Online.Booking.App.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uts.Online.Booking.App.DAO.UserDAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.uts.Online.Booking.App.model.User;


import java.util.List;

@Service 
public class UserService {
    
    private final UserDAO userDAO;

    @PersistenceContext
    private EntityManager entityManager;

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

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Update the role in users table
        user.setRole(role);
        userDAO.save(user);

        //Clear any old roles in tables
        entityManager.createNativeQuery("DELETE FROM dbo.admins WHERE userId = :id")
                .setParameter("id", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM dbo.players WHERE userId = :id")
                .setParameter("id", userId)
                .executeUpdate();

        //Add role to table
        if ("ROLE_ADMIN".equals(role)) {
            entityManager.createNativeQuery("INSERT INTO dbo.admins (userId) VALUES (:id)")
                    .setParameter("id", userId)
                    .executeUpdate();

        } else if ("ROLE_PLAYER".equals(role)) {
            entityManager.createNativeQuery("""
                IF NOT EXISTS (SELECT 1 FROM dbo.players WHERE userId = :id)
                    INSERT INTO dbo.players (userId, address, payment_id, creditBalance)
                    VALUES (:id, '', NULL, 0.0)
            """)
            .setParameter("id", userId)
            .executeUpdate();
        }

        entityManager.flush();
    }
}
