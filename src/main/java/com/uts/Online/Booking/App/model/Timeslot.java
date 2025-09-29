package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "Timeslots")
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeslotId;
    private LocalTime startTime;
    private LocalTime endTime;

    public Timeslot() {}

    public Long getTimeslotId() { return timeslotId; }
    public void setTimeslotId(Long timeslotId) { this.timeslotId = timeslotId; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}