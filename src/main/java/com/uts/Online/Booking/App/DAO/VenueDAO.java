package com.uts.Online.Booking.App.DAO;

import com.uts.Online.Booking.App.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueDAO extends JpaRepository<Venue, Long> {
}