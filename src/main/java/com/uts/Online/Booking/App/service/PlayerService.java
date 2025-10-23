package com.uts.Online.Booking.App.service;

import com.uts.Online.Booking.App.DAO.PlayerDAO;
import com.uts.Online.Booking.App.model.Player;

import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final PlayerDAO playerDAO;

    PlayerService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    public double getCreditBalance(String email){
        //get player by email
        Player player = playerDAO.findByEmail(email);

        if(player != null){
            return player.getCreditBalance();
        } else{
            return 0.0;
        }
    }
}
