package com.uts.Online.Booking.App.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingConfirmationEmail(User user, List<Booking> bookings, Double totalAmount) {
        if (user == null || bookings == null || bookings.isEmpty()) return;

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(user.getFirstName() != null ? user.getFirstName() : "there").append(",\n\n");
        body.append("Your booking has been successfully confirmed!\n\n");

        body.append("Details:\n");
        for (Booking b : bookings) {
            String venueName = null;
            String courtName = null;
            String timeLabel = null;

            try {
                venueName = (b.getCourt() != null && b.getCourt().getVenue() != null)
                        ? b.getCourt().getVenue().getVenueName()
                        : null;
                courtName = (b.getCourt() != null) ? b.getCourt().getCourtName() : null;
                timeLabel = (b.getTimeslot() != null && b.getTimeslot().getStartTime() != null)
                        ? b.getTimeslot().getStartTime().toString()
                        : null;
            } catch (Exception ignored) {}

            body.append("• Date: ").append(b.getBookingDate()).append('\n');
            if (timeLabel != null) body.append("  Time: ").append(timeLabel).append('\n');
            if (venueName != null) body.append("  Venue: ").append(venueName).append('\n');
            if (courtName != null) body.append("  Court: ").append(courtName).append('\n');
            body.append('\n');
        }

        if (totalAmount != null) {
            body.append("Total amount: $").append(String.format("%.2f", totalAmount)).append("\n\n");
        }

        body.append("Thank you for booking with CourtBooker!\n")
            .append("— CourtBooker Team\n");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("CourtBooker – Booking Confirmation");
        msg.setText(body.toString());
        mailSender.send(msg);
    }
}