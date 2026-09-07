package com.readora.backend.repository;

import com.readora.backend.entity.EmailVerificationToken;
import com.readora.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findAllByUserAndUsedFalse(User user);
}
