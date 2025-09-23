package com.uts.Online.Booking.App.DAO;

import com.uts.Online.Booking.App.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourtDAO extends JpaRepository<Court, Long> {
    List<Court> findByVenueVenueId(Long venueId);
}