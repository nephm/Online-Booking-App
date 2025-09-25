package com.uts.Online.Booking.App.DAO;


import com.uts.Online.Booking.App.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentDAO extends JpaRepository<Payment, Integer> {
    List<Payment> findByPaymentId(Integer paymentId);

    List<Payment> findByBookingId(Integer bookingId);

    List<Payment> findByUserId(Integer userId);
}
