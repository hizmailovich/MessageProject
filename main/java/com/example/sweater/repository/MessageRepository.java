package com.example.sweater.repository;

import com.example.sweater.entity.Message;
import com.example.sweater.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {

    /**
     * Method to find all messages
     *
     * @param pageable pageable
     * @return list of messages
     */
    Page<Message> findAll(Pageable pageable);

    /**
     * Method to find messages by tag
     *
     * @param tag tag
     * @param pageable pageable
     * @return list of messages
     */
    Page<Message> findAllByTag(String tag, Pageable pageable);

    /**
     * Method to find messages by username
     *
     * @param author user
     * @param pageable pageable
     * @return list of messages
     */
    Page<Message> findAllByAuthor(User author, Pageable pageable);
}
