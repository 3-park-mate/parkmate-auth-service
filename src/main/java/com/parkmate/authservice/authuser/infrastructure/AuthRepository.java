package com.parkmate.authservice.authuser.infrastructure;

import com.parkmate.authservice.authuser.domain.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AuthUser> findByUserUuid(String userUuid);
}
