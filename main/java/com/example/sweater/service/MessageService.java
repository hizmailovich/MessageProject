package com.example.sweater.service;

import com.example.sweater.entity.Message;
import com.example.sweater.entity.User;
import com.example.sweater.repository.MessageRepository;
import com.example.sweater.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<Message> getAllMessages(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    public Page<Message> getMessagesByTag(String tag, Pageable pageable) {
        return messageRepository.findAllByTag(tag, pageable);
    }

    public Message getMessagesById(long id) {
        return messageRepository.findById(id).orElseThrow();
    }

    public void addNewMessage(String text, String tag, MultipartFile file) throws IOException {
        User user = findAuthor();
        Message message = new Message(text, tag, user);
        if (!file.getOriginalFilename().equals("")) {
            message.setImage(file.getBytes());
        }
        messageRepository.save(message);
    }

    public void editMessage(long id, String text, String tag){
        User user = findAuthor();
        Message message = messageRepository.findById(id).orElseThrow();
        message.setText(text);
        message.setTag(tag);
        message.setAuthor(user);
        messageRepository.save(message);
    }

    public void deleteMessage(long id) {
        Message message = messageRepository.findById(id).orElseThrow();
        messageRepository.delete(message);
    }

    public Page<Message> getMessagesByAuthor(long id, Pageable pageable) {
        User author = userRepository.findById(id).orElseThrow();
        return messageRepository.findAllByAuthor(author, pageable);
    }

    private User findAuthor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findUserByUsername(auth.getName());
    }
}
