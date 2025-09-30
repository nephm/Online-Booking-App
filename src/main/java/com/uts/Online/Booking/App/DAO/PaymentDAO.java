package com.uts.Online.Booking.App.DAO;


import com.uts.Online.Booking.App.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentDAO extends JpaRepository<Payment, Long> {
    List<Payment> findByPaymentId(Long paymentId);

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByUserId(Long userId);
}
