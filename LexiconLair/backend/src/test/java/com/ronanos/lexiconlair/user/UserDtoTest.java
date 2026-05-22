package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserDtoTest {

    @Test
    void userResponseMapsPublicUserFieldsAndExcludesPassword() {
        User user = new User("admin", "encoded-secret", "admin@example.com", "Admin", "User");
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 11, 0));
        user.setCreatedBy(7L);
        user.setUpdatedBy(8L);
        user.setRole("ADMIN");

        UserResponse response = UserResponse.from(user);

        assertEquals(1L, response.id());
        assertEquals("admin", response.username());
        assertEquals("admin@example.com", response.email());
        assertEquals("Admin", response.firstName());
        assertEquals("User", response.lastName());
        assertEquals("ADMIN", response.role());
        assertEquals(7L, response.createdBy());
        assertEquals(8L, response.updatedBy());
        assertFalse(Arrays.stream(UserResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch("password"::equals));
    }
}
