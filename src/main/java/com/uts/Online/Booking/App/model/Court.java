package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Courts", schema ="dbo")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="courtId")
    private Long court_id;
    @Column(name="courtName")
    private String court_name;
    @Column(name="courtType")
    private String court_type;
    
    private String location;
    @Column(name="hourlyRate")
    private double hourly_rate;

    @Transient
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "venueId", nullable = false) // foreign key
    private Venue venue;

    public Court() {}

    public Court(Long id, String name, boolean available) {
        this.court_id = id;
        this.court_name = name;
        this.available = available;
    }

    // Getters and setters
    public Long getCourt_id() {
        return court_id;
    }

    public void setCourt_id(Long court_id) {
        this.court_id = court_id;
    }

    public String getCourt_name() {
        return court_name;
    }

    public void setCourt_name(String court_name) {
        this.court_name = court_name;
    }

    public String getCourt_type() {
        return court_type;
    }

    public void setCourt_type(String court_type) {
        this.court_type = court_type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getHourly_rate() {
        return hourly_rate;
    }

    public void setHourly_rate(double hourly_rate) {
        this.hourly_rate = hourly_rate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}