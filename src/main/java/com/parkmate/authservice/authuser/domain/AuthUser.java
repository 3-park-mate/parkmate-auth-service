package com.parkmate.authservice.authuser.domain;

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
@Table(name = "auth_user")
public class AuthUser extends BaseEntity implements UserDetails {

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

    @Comment("비밀번호 (일반 로그인만 사용)")
    @Column(length = 100)
    private String password;

    @Comment("로그인 방식 (NORMAL, SOCIAL)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginType loginType;

    @Comment("소셜 제공자 (KAKAO 등, 일반 로그인은 NONE)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider = SocialProvider.NONE;

    @Comment("계정 잠금 여부")
    @Column(nullable = false)
    private boolean accountLocked = false;

    @Builder
    private AuthUser(Long id,
                     String userUuid,
                     String email,
                     String password,
                     LoginType loginType,
                     SocialProvider provider,
                     boolean accountLocked) {
        this.id = id;
        this.userUuid = userUuid;
        this.email = email;
        this.password = password;
        this.loginType = loginType;
        this.provider = provider;
        this.accountLocked = accountLocked;
    }

    public void lockAccount() {
        this.accountLocked = true;
    }

    public boolean isAccountLocked() {
        return this.accountLocked;
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