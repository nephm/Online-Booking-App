package com.uts.Online.Booking.App.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courtId;
    private String courtName;
    private String courtType;
    private String location;
    private double hourlyRate;

    @Transient
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "venueId")
    private Venue venue;

    public Court() {}

    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    
    public String getCourtType() { return courtType; }
    public void setCourtType(String courtType) { this.courtType = courtType; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
}