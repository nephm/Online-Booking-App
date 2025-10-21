package com.uts.Online.Booking.App.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendConfirmationEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Booking Confirmation");
        message.setText("Thank you for booking with CourtBooker! Your booking has been confirmed.");

        mailSender.send(message);
        System.out.println("✅ Confirmation email sent to " + to);
    }
}
