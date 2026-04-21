package com.ronanos.lexiconlair.user.web;

import java.time.LocalDateTime;
import java.util.List;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@SessionAttributes("name")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping(value = "list-users")
    public String listAllUsers(ModelMap model) {
        List<User> userList = userRepository.findAll();
        model.put("users", userList);
        return "user/listUsers";
    }

    @RequestMapping(value = "add-user", method = RequestMethod.GET)
    public String showCreateUserForm(ModelMap model) {
        User user = new User("", "", "", "", "");
        model.addAttribute("user", user);
        return "user/addUser";
    }

    @RequestMapping(value = "add-user", method = RequestMethod.POST)
    public String createUser(ModelMap model, @Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "user/addUser";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLastUpdated(LocalDateTime.now());
        user.setUpdatedBy(getCurrentUserId());
        model.addAttribute("user", user);
        userRepository.save(user);
        logger.info("Created user {}", user.getUsername());
        return "redirect:list-users";
    }

    @PostMapping("delete-user")
    public String deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return "redirect:list-users";
    }

    @RequestMapping(value = "update-user")
    public String showUpdateUserForm(@RequestParam Long id, ModelMap model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword("");
        model.addAttribute("user", user);
        return "user/addUser";
    }

    @RequestMapping(value = "update-user", method = RequestMethod.POST)
    public String updateUser(ModelMap model, @Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "user/addUser";
        }
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setUpdatedBy(getCurrentUserId());
        user.setLastUpdated(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Updated user {}", user.getUsername());

        return "redirect:list-users";
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
