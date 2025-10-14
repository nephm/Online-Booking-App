package com.uts.Online.Booking.App.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Admin;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.DAO.AdminDAO;
import com.uts.Online.Booking.App.DAO.PlayerDAO;
import com.uts.Online.Booking.App.model.User;

@Service
public class CustomerDetailsService implements UserDetailsService {
    private final UserDAO userDAO;
    private final PlayerDAO playerDAO;

    public CustomerDetailsService(UserDAO userDAO, AdminDAO adminDAO, PlayerDAO playerDAO) {
        this.userDAO = userDAO;
        this.playerDAO = playerDAO;
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

        //check user types and set roles
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(u.getRole()));

        return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), authorities);
    }

    public String getUsername(){
        return userDAO.findById((Long) 1L).map(User::getFirstName).orElse("defaultUser");
    }

    public Player findById(Long id){
        return playerDAO.findById(id).orElse(null);
    }

    public Player findByEmail(String email){
        return playerDAO.findByEmail(email);
    }
}
