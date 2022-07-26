package com.example.sweater.controller;

import com.example.sweater.entity.Role;
import com.example.sweater.entity.User;
import com.example.sweater.entity.dto.CaptchaResponseDto;
import com.example.sweater.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    @Value("${recaptcha.secret}")
    private String secret;

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/registration")
    public String showRegisterForm(Model model) {
        return "registration";
    }

    @PostMapping("/registration")
    public String addNewUser(@RequestParam("g-recaptcha-response") String captchaResponse,
                             User user,
                             Model model) {
        String url = String.format(CAPTCHA_URL, secret, captchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Fill captcha!");
            logger.warn("Captcha field is empty!");
            return "registration";
        }
        if (user.getUsername() == null || user.getPassword() == null) {
            model.addAttribute("error", "All fields must be filled!");
            logger.warn("Fields are empty!");
            return "registration";
        }
        if (!user.getPassword().equals(user.getPasswordConf())) {
            model.addAttribute("error", "Confirmation password doesn't match the entered password!");
            logger.warn("Passwords do not match!");
            return "registration";
        }
        User userFromDB = userService.getUserByUsername(user.getUsername());
        if (userFromDB != null) {
            model.addAttribute("error", "User exists!");
            logger.warn("User exists!");
            return "registration";
        }
        userService.prepareUserForActivation(user);
        logger.info("User successfully saved!");
        model.addAttribute("info", "An activation code has been sent to your email. Follow the suggested link.");
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(@PathVariable String code,
                           Model model) {
        boolean isActive = userService.activateUser(code);
        if (isActive) {
            model.addAttribute("info", "User successfully activated!");
        } else {
            model.addAttribute("info", "Activation code is not found!");
        }
        return "login";
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public String getUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{user}")
    public String showEditForm(@PathVariable User user,
                               Model model) {
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "users-edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users")
    public String editUsers(@RequestParam("user_id") User user,
                            @RequestParam String username,
                            @RequestParam Map<String, String> form,
                            Model model) {
        userService.editUser(user, username, form);
        logger.info("User successfully edited");
        return "redirect:/users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("deleteUser")
    public String editUsers(@RequestParam(name = "delete_id", defaultValue = "") String deleteId,
                            Model model) {
        if (!deleteId.equals("")) {
            userService.deleteUser(Long.parseLong(deleteId));
            logger.info("User successfully deleted");
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}/profile")
    public String showProfile(@PathVariable(value = "id") long id,
                              Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/{id}/profile")
    public String editProfile(@PathVariable(value = "id") long id,
                              @RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              Model model) {
        userService.updateUser(id, username, email, password);
        logger.info("User successfully edited");
        return "redirect:/home";
    }

    @GetMapping("/{id}/subscribe")
    public String subscribe(@PathVariable(value = "id") long id,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        User user = userService.getUserById(id);
        userService.subscribe(currentUser, user);
        return "redirect:/" + id + "/messages";
    }

    @GetMapping("/{id}/unsubscribe")
    public String unsubscribe(@PathVariable(value = "id") long id,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        User user = userService.getUserById(id);
        userService.unsubscribe(currentUser, user);
        return "redirect:/" + id + "/messages";
    }

    @GetMapping("/{id}/subscriptions")
    public String showSubscriptions(@PathVariable(value = "id") long id,
                                    Model model) {
        Set<User> subscriptions = userService.getUserById(id).getSubscriptions();
        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions";
    }

    @GetMapping("/{id}/subscribers")
    public String showSubscribers(@PathVariable(value = "id") long id,
                                  Model model) {
        Set<User> subscribers = userService.getUserById(id).getSubscribers();
        model.addAttribute("subscribers", subscribers);
        return "subscribers";
    }
}
