package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Timeslots", schema ="dbo")
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeslot_id;

    private String start_time;
    private String end_time;

    public Timeslot() {}

    public Timeslot(Long timeslot_id, String start_time, String end_time) {
        this.timeslot_id = timeslot_id;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public Long getTimeslot_id() {
        return timeslot_id;
    }

    public void setTimeslot_id(Long timeslot_id) {
        this.timeslot_id = timeslot_id;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}