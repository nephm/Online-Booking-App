package com.uts.Online.Booking.App.DAO;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.User;

import org.springframework.lang.NonNull;
@Repository
public interface PlayerDAO extends JpaRepository<Player, Integer> {
    boolean existsById(@NonNull Long Id);

    Optional<Player> findByEmail(String email);
    
}
