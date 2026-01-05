package sopt.comfit.global.security.info;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sopt.comfit.user.domain.ERole;
import sopt.comfit.user.dto.UserSecurityForm;

import java.util.Collection;
import java.util.Collections;

public record UserPrincipal(
        Long userId,

        String password,

        ERole role,

        Collection<? extends GrantedAuthority> authorities
) implements UserDetails{

    public static UserPrincipal create(UserSecurityForm securityForm){
        return new UserPrincipal(
                securityForm.getUserId(),
                securityForm.getPassword(),
                securityForm.getRole(),
                Collections.singleton(new SimpleGrantedAuthority(securityForm.getRole().getSecurityRole()))
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getUsername() {
        return this.userId.toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
