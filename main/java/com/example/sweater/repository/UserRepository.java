package com.example.sweater.repository;

import com.example.sweater.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Method to find user by username
     *
     * @param username username
     * @return user
     */
    User findUserByUsername(String username);

    /**
     * Method to find user by activation code
     *
     * @param code activation code
     * @return user
     */
    User findUserByActivationCode(String code);
}
