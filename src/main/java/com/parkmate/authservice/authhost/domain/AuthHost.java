package com.parkmate.authservice.authhost.domain;

import com.parkmate.authservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_host")
public class AuthHost extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("호스트 고유 PK")
    private Long id;

    @Comment("호스트 UUID")
    @Column(nullable = false, unique = true, length = 36)
    private String hostUuid;

    @Comment("이메일")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Comment("비밀번호")
    @Column(nullable = false, length = 100)
    private String password;

    @Comment("계정 잠금 여부")
    @Column(nullable = false)
    private boolean accountLocked = false;

    @Builder
    private AuthHost(Long id,
                     String hostUuid,
                     String email,
                     String password,
                     boolean accountLocked) {
        this.id = id;
        this.hostUuid = hostUuid;
        this.email = email;
        this.password = password;
        this.accountLocked = accountLocked;
    }

    public void lockAccount() {
        this.accountLocked = true;
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
        return !this.accountLocked;
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