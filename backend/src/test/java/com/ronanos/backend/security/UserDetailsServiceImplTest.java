package com.ronanos.backend.security;

import com.ronanos.backend.user.domain.User;
import com.ronanos.backend.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadsUserByUsernameWithUserRole() {
        User user = new User("admin", "encoded-password", "admin@example.com", "Admin", "User");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        UserDetails details = service.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertEquals("encoded-password", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void throwsWhenUsernameIsUnknown() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}
