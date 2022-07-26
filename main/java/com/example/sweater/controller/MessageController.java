package com.example.sweater.controller;

import com.example.sweater.entity.Message;
import com.example.sweater.entity.User;
import com.example.sweater.service.MessageService;
import com.example.sweater.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class MessageController {
    private static final Logger logger = LogManager.getLogger(MessageController.class);

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        return "add";
    }

    @PostMapping("/add")
    public String addNewMessage(@RequestParam String text,
                                @RequestParam(name = "tag", required = false, defaultValue = "none") String tag,
                                @RequestParam(name = "file", required = false, defaultValue = "") MultipartFile file,
                                Model model) throws IOException {
        if (text.isEmpty()) {
            model.addAttribute("error", "Please fill text!");
            logger.warn("Fields are empty!");
            return "add";
        }
        messageService.addNewMessage(text, tag, file);
        logger.info("Message successfully saved");
        return "redirect:/home";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(value = "id") long id,
                               Model model) {
        Message message = messageService.getMessagesById(id);
        model.addAttribute("message", message);
        return "edit";
    }

    @PostMapping("/{id}/edit")
    public String editMessage(@PathVariable(value = "id") long id,
                              @RequestParam String text,
                              @RequestParam String tag,
                              Model model) {
        messageService.editMessage(id, text, tag);
        logger.info("Message successfully edited");
        return "redirect:/home";
    }

    @GetMapping("/{id}/messages")
    public String showMessages(@PathVariable(value = "id") long id,
                               @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                               Model model) {
        Page<Message> messages = messageService.getMessagesByAuthor(id, pageable);
        User user = userService.getUserById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());

        model.addAttribute("pages", Arrays.stream(new int[messages.getTotalPages()])
                .map(el -> 1)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        model.addAttribute("myMessages", messages);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));
        return "messages";
    }

    @PostMapping("delete")
    public String deleteMessage(@RequestParam(name = "delete_id", defaultValue = "") String deleteId,
                                Model model) {
        if (!deleteId.equals("")) {
            messageService.deleteMessage(Long.parseLong(deleteId));
            logger.info("Message successfully deleted");
        }
        return "redirect:/home";
    }
}
