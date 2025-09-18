package com.uts.Online.Booking.App.model;

public class Court {
    private long id;
    private String name;
    private boolean available;

    public Court(long id, String name, boolean available) {
        this.id = id;
        this.name = name;
        this.available = available;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }
 
}
