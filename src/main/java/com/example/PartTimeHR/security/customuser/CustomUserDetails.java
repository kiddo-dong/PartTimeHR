package com.example.PartTimeHR.security.customuser;

import com.example.PartTimeHR.auth.domain.Account;
import com.example.PartTimeHR.employer.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final Role role;  // Enum 그대로 저장
    private final Long id;
    private final String password;

    public CustomUserDetails(Account account) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.role = account.getRole();
        this.password = account.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name())); // Enum → 문자열
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
