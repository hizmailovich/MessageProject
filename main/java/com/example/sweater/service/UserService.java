package com.example.sweater.service;

import com.example.sweater.entity.Role;
import com.example.sweater.entity.User;
import com.example.sweater.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public void updateUser(long id, String username, String email, String password) {
        User user = userRepository.findById(id).orElseThrow();
        boolean isEmailChanged = email != null && !email.equals(user.getEmail()) && !email.isEmpty() ||
                user.getEmail() != null && !user.getEmail().equals(email);
        if (isEmailChanged) {
            user.setEmail(email);
            user.setActivationCode(UUID.randomUUID().toString());
        }
        if (!password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (!username.isEmpty()) {
            user.setUsername(username);
        }
        addNewUser(user);
        if (isEmailChanged) {
            sendMessage(user);
        }
    }

    public void prepareUserForActivation(User user) {
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sendMessage(user);
        addNewUser(user);
    }

    public void sendMessage(User user) {
        if (!user.getEmail().isEmpty()) {
            String message = String.format("Hello, %s!\n" +
                            "Welcome to Sweater! Please, visit next link: http://localhost:8080/sweater/activate/%s",
                    user.getUsername(), user.getActivationCode());
            mailSender.send(user.getEmail(), "Activation code", message);
        }
    }

    public void addNewUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
    }

    public void editUser(User user, String username, Map<String, String> form) {
        user.setUsername(username);
        user.getRoles().clear();
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        addNewUser(user);
    }

    public boolean activateUser(String code) {
        User user = userRepository.findUserByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }

    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepository.save(user);
    }

    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepository.save(user);
    }
}
