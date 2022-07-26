package com.example.sweater.service;

import com.example.sweater.entity.Role;
import com.example.sweater.entity.User;
import com.example.sweater.repository.UserRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Collections;
import java.util.UUID;



@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MailSender mailSender;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void prepareUserForActivation() {
        User user = new User();
        user.setEmail("some@mail.ru");

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.sendMessage(user);
        userService.addNewUser(user);

        Assertions.assertNotNull(user.getActivationCode());
        Assertions.assertTrue(CoreMatchers.is(user.getRoles()).matches(Collections.singleton(Role.USER)));
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(mailSender, Mockito.times(1))
                .send(ArgumentMatchers.eq(user.getEmail()),
                        ArgumentMatchers.eq("Activation code"),
                        ArgumentMatchers.eq("Hello, null!"));
    }
}