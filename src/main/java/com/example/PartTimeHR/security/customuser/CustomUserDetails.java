package com.example.PartTimeHR.security.customuser;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employer.domain.Employer;
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

    public CustomUserDetails(Employer employer) {
        this.id = employer.getId();
        this.email = employer.getEmail();
        this.role = employer.getRole();
        this.password = employer.getPassword();
    }

    public CustomUserDetails(Employee employee) {
        this.id = employee.getId();
        this.email = employee.getEmail();
        this.role = employee.getRole();
        this.password = employee.getPassword();
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