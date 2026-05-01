package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryDataJpaTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsernameReturnsPersistedUser() {
        User user = new User("admin", "password123", "admin@example.com", "Admin", "User");
        User saved = userRepository.saveAndFlush(user);

        Optional<User> actual = userRepository.findByUsername("admin");

        assertTrue(actual.isPresent());
        assertEquals(saved.getId(), actual.get().getId());
        assertEquals("admin@example.com", actual.get().getEmail());
    }

    @Test
    void findByUsernameReturnsEmptyForUnknownUser() {
        assertTrue(userRepository.findByUsername("missing").isEmpty());
    }
}
