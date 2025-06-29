package com.codegeneration.banking.api.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegeneration.banking.api.service.interfaces.UserService;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        System.out.println(userRepository.findAll());
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByPage(Number pageNumber, Number limit) {
        int MAX_LIMIT = 100; //No reason to have it be a number and convert it to int, 100 is a reasonable limit
        if (limit.intValue() > MAX_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested limit exceeds maximum allowed");
        }
        //Use pageable to get the users in manageable pages instead of all at once
        Pageable pageable = PageRequest.of(pageNumber.intValue()-1, limit.intValue());
        return userRepository.findAll(pageable).getContent();
    }
    @Override
    @Transactional(readOnly = true)
    public List<User> getUserById(Number id) {
        if (id.intValue() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not request a user with an id less than 1");
        }
        return userRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {

        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getDisabledUsersByPage(Number pageNumber, Number limit) {
        int MAX_LIMIT = 100;
        if (limit.intValue() > MAX_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested limit exceeds maximum allowed");
        }
        
        Pageable pageable = PageRequest.of(pageNumber.intValue() - 1, limit.intValue());
        return userRepository.findByEnabled(false, pageable).getContent();
    }    @Override
    @Transactional
    public User enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already enabled");
        }
        
        user.setEnabled(true);
        return userRepository.save(user);
    }
}