package com.ronanos.lexiconlair.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@SessionAttributes("name")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "list-users")
    public String listAllUsers(ModelMap model) {
        List<User> userList = userRepository.findAll();
        model.put("users", userList);
        return "listUsers";
    }

    @RequestMapping(value = "add-user", method = RequestMethod.GET)
    public String showNewUserPage(ModelMap model) {
        User user = new User("", "", "", "", "");
        model.addAttribute("user", user);
        return "addUser";
    }

    @RequestMapping(value = "add-user", method = RequestMethod.POST)
    public String addUser(ModelMap model, @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "addUser";
        }
        user.setLastUpdated(LocalDateTime.now());
        // TODO change the ID to be the logged in user id.
        user.setUpdatedBy(0L);
        model.addAttribute("user", user);
        userRepository.save(user);
        return "redirect:list-users";
    }

    @RequestMapping(value = "delete-user")
    public String deleteUser(@RequestParam int id) {
        userRepository.deleteById(id);
        return "redirect:list-users";
    }

    @RequestMapping(value = "update-user")
    public String showUpdateUserPage(@RequestParam int id, ModelMap model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        model.addAttribute("user", user);
        return "addUser";
    }

    @RequestMapping(value = "update-user", method = RequestMethod.POST)
    public String updateUser(ModelMap model, @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:list-users";
        }
        // TODO replace with the correct id
        user.setUpdatedBy(0L);
        user.setLastUpdated(LocalDateTime.now());
        userRepository.save(user);

        return "redirect:list-users";
    }

}
