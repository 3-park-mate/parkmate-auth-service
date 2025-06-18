package com.parkmate.authservice.authhost.infrastructure;

import com.parkmate.authservice.authhost.domain.AuthHost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthHostRepository extends JpaRepository<AuthHost, Long> {

    Optional<AuthHost> findByEmail(String email);

    boolean existsByEmail(String email);
}