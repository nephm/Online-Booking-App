package com.uts.Online.Booking.App.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.DAO.AdminDAO;
import com.uts.Online.Booking.App.model.Admin;
import com.uts.Online.Booking.App.model.User;

@Service
public class UserService implements UserDetailsService {
    private final UserDAO userDAO;
    private final AdminDAO adminDAO;

    public UserService(UserDAO userDAO, AdminDAO adminDAO) {
        this.userDAO = userDAO;
        this.adminDAO = adminDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User u = userDAO.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        //check if acount is activated
        if(!u.isActive()){
            throw new UsernameNotFoundException("User not activated: " + email);
        }

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(email);
        builder.password(u.getPassword());

        //set roles
        if (adminDAO.existsById(u.getId())) {
            builder.roles("ADMIN");
        } else {
            builder.roles("USER");
        }

        return builder.build();
    }
}
