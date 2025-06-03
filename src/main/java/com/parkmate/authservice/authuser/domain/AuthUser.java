package com.parkmate.authservice.authuser.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_user")
public class AuthUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("회원 고유 PK")
    private Long id;

    @Comment("회원 UUID")
    @Column(nullable = false, unique = true, length = 36)
    private String userUuid;

    @Comment("이메일")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Comment("비밀번호")
    @Column(nullable = false, length = 100)
    private String password;

    @Comment("로그인 실패 횟수")
    @Column(nullable = true)
    private Integer loginFailCount;

    @Comment("계정 잠금 여부")
    @Column(nullable = false)
    private boolean accountLocked = false;

    @Builder
    private AuthUser(Long id,
                     String userUuid,
                     String email,
                     String password,
                     Integer loginFailCount,
                     boolean accountLocked) {

        this.id = id;
        this.userUuid = userUuid;
        this.email = email;
        this.password = password;
        this.loginFailCount = loginFailCount;
        this.accountLocked = accountLocked;
    }

    public void increaseFailCount() {
        this.loginFailCount++;
    }

    public void lockAccount() {
        this.accountLocked = true;
    }

    public void resetLoginStatus() {
        this.loginFailCount = 0;
        this.accountLocked = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}