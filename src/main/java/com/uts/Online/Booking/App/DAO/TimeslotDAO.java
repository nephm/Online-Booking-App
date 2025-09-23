package com.uts.Online.Booking.App.DAO;

import com.uts.Online.Booking.App.model.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeslotDAO extends JpaRepository<Timeslot, Long> {
}