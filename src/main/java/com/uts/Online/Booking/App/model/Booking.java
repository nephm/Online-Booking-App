package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bookings")
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

    @Transient
    public boolean canBeModified(){
        //can only modify bookings that are ato least a day before 
        if (this.bookingDate == null || this.timeslot == null || this.timeslot.getStartTime() == null) {
            return false;
        }
        LocalDateTime bookingDateTime = LocalDateTime.of(
            this.bookingDate,
            this.timeslot.getStartTime()
        );
        return LocalDateTime.now().plusHours(24).isBefore(bookingDateTime);
    }
}