package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Bookings", schema ="dbo")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "courtId")
    private Court court;

    @ManyToOne
    @JoinColumn(name = "timeslotId")
    private Timeslot timeslot;

    private LocalDate bookingDate;
    private Long userId;
    private String status;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
    
    public Timeslot getTimeslot() { return timeslot; }
    public void setTimeslot(Timeslot timeslot) { this.timeslot = timeslot; }
    
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}