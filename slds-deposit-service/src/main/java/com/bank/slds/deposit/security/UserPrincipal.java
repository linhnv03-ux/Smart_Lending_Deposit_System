package com.bank.slds.deposit.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String userId;
    private final String username;
    private final String role;
    private final String branchCode;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(String userId, String username, String role, String branchCode) {
        String authority = role != null && !role.startsWith("ROLE_") ? "ROLE_" + role : (role != null ? role : "ROLE_USER");
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(authority));

        return new UserPrincipal(userId, username, role, branchCode, authorities);
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
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
