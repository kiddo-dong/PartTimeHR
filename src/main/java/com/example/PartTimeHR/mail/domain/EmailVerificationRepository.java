package com.example.PartTimeHR.mail.domain;

import com.example.PartTimeHR.mail.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    @Query("select ev from EmailVerification ev join fetch ev.employer where ev.token = :token")
    Optional<EmailVerification> findByTokenWithEmployer(@Param("token") String token);

    void deleteByEmail(String email);

    Optional<EmailVerification> findByEmployerId(Long employerId);
}
