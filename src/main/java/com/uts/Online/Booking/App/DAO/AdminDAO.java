package com.uts.Online.Booking.App.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.uts.Online.Booking.App.model.Admin;

@Repository

public interface AdminDAO extends JpaRepository<Admin, Integer> {
    boolean existsById(@NonNull Long Id);
    
}
