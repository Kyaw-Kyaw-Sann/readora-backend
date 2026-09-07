package com.readora.backend.repository;

import com.readora.backend.entity.PasswordResetOtp;
import com.readora.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    List<PasswordResetOtp> findAllByUserAndUsedFalse(User user);

    Optional<PasswordResetOtp> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}
