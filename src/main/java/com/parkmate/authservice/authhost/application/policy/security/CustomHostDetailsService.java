package com.parkmate.authservice.authhost.application.policy.security;

import com.parkmate.authservice.authhost.infrastructure.AuthHostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("hostDetailsService")
@RequiredArgsConstructor
public class CustomHostDetailsService implements UserDetailsService {

    private final AuthHostRepository authHostRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return authHostRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 호스트입니다: " + email));
    }
}