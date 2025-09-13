package com.uts.Online.Booking.App.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uts.Online.Booking.App.model.Admin;
import java.util.Optional;

@Repository
public interface AdminDAO extends JpaRepository<Admin, Long> {
    boolean existsByAdminId(Long userId);
    
}
