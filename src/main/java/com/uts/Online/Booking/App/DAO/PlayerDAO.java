package com.uts.Online.Booking.App.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uts.Online.Booking.App.model.Player;
import org.springframework.lang.NonNull;
@Repository
public interface PlayerDAO extends JpaRepository<Player, Long> {
    boolean existsById(@NonNull Long Id);

    Player findByEmail(String email);
}
