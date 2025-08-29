package com.everbit.everbit.user.service;

import com.everbit.everbit.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserRepository userRepository;

    @Test
    void createOrFindUser() {
    }

    @Test
    void findUserByUserId() {
    }

    @Test
    void findUserByUsername() {
    }

    @Test
    void saveUser() {
    }

    @Test
    void findUsersWithActiveBots() {
    }
}