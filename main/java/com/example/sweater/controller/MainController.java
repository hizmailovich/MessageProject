package com.example.sweater.controller;

import com.example.sweater.entity.Message;
import com.example.sweater.entity.User;
import com.example.sweater.service.MessageService;
import com.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String start(Model model) {
        return "start";
    }

    @GetMapping("/home")
    public String getAllMessages(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                                 Model model) {
        Page<Message> messages = messageService.getAllMessages(pageable);
        model.addAttribute("messages", messages);
        model.addAttribute("pages", Arrays.stream(new int[messages.getTotalPages()])
                .map(el -> 1)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        if (user != null) {
            if (user.isAdmin()) {
                model.addAttribute("admin", user);
            }
            model.addAttribute("user", user);
        }
        return "home";
    }

    @PostMapping("/home")
    public String filterMessage(@RequestParam String filter,
                                Pageable pageable,
                                Model model) {
        if (filter == null || filter.isEmpty()) {
            return "redirect:/home";
        }
        Page<Message> messages = messageService.getMessagesByTag(filter, pageable);
        model.addAttribute("filter", filter);
        model.addAttribute("messages", messages);
        return "home";
    }
}