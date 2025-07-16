package com.everbit.everbit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManager {
    private final UserService userService;

    @Transactional(readOnly = true)
    public UserResponse getUserResponse(String username) {
        User user = userService.findUserByUsername(username);
        return UserResponse.from(user);
    }
}
