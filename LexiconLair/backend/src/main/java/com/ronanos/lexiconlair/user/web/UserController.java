package com.ronanos.lexiconlair.user.web;

import java.time.LocalDateTime;
import java.util.List;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.dto.UserCreateRequest;
import com.ronanos.lexiconlair.user.dto.UserResponse;
import com.ronanos.lexiconlair.user.dto.UserUpdateRequest;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                request.firstName(),
                request.lastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(getCurrentUserId());
        User saved = userRepository.save(user);
        logger.info("Created user {}", saved.getUsername());
        return UserResponse.from(saved);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User user = new User(
                request.username(),
                request.password(),
                request.email(),
                request.firstName(),
                request.lastName());
        user.setId(id);
        if (request.password() == null || request.password().isBlank()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        user.setCreatedAt(existingUser.getCreatedAt());
        user.setCreatedBy(existingUser.getCreatedBy());
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        logger.info("Updated user {}", saved.getUsername());
        return UserResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        return userRepository.findByUsername(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }

}
