package com.api.barberia.config;

import java.util.Locale;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.api.barberia.repository.UserRepository;

/**
 * Uses the user and role already stored by the API as the staff login source.
 */
@Service
public class BarberUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public BarberUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada."));
        String role = user.getRolesId().getName().toUpperCase(Locale.ROOT);
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .build();
    }
}
