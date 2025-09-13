package com.uts.Online.Booking.App.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uts.Online.Booking.App.model.Player;
import java.util.Optional;

@Repository
public interface PlayerDAO extends JpaRepository<Player, Long> {
    boolean existsByPlayerId(Long userId);
    
}
