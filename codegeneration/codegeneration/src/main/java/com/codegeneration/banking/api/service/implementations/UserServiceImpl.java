package com.codegeneration.banking.api.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegeneration.banking.api.service.interfaces.UserService;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.entity.User;

import java.util.ArrayList;
import java.util.List;

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
        //later
        return new ArrayList<>();
    }
}