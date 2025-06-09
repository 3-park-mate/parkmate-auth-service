package com.parkmate.authservice.common.security.jwt;

import com.parkmate.authservice.authuser.domain.AuthUser;
import com.parkmate.authservice.authuser.infrastructure.AuthRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, AuthRepository authRepository) {
        this.jwtProvider = jwtProvider;
        this.authRepository = authRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 화이트리스트 경로 지정 (토큰 없이 접근 허용)
        return path.startsWith("/api/v1/user/login")
                || path.startsWith("/api/v1/user/logout")
                || path.startsWith("/api/v1/user/register")
                || path.startsWith("/api/v1/host/login")
                || path.startsWith("/api/v1/host/logout")
                || path.startsWith("/api/v1/host/register")
                || path.startsWith("/api/v1/sendVerification")
                || path.startsWith("/api/v1/verifyCode")
                || path.startsWith("/api/v1/socialLogin")
                || path.startsWith("/api/v1/socialRegister");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {

                String email = jwtProvider.extractSubject(token);

                AuthUser user = authRepository.findByEmail(email)
                        .orElse(null);

                if (user != null) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUserUuid(),
                                    null,
                                    Collections.emptyList()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}