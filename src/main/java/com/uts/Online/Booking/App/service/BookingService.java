package com.uts.Online.Booking.App.service;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingDAO bookingDAO;
    @Autowired
    private CourtDAO courtDAO;
    @Autowired
    private TimeslotDAO timeslotDAO;

    public void createBooking(Long courtId, Long timeslotId, LocalDate bookingDate, long userId) {
    Court court = courtDAO.findById(courtId).orElseThrow();
    Timeslot timeslot = timeslotDAO.findById(timeslotId).orElseThrow();
    Booking booking = new Booking();
    booking.setCourt(court);
    booking.setTimeslot(timeslot);
    booking.setBooking_date(bookingDate);
    booking.setStatus("BOOKED");
    booking.setUserid(userId);
    bookingDAO.save(booking);
}
}