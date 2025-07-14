package com.everbit.everbit.user.dto;

import com.everbit.everbit.user.entity.User;

public record UserDto(
    String username,
    String role
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getUsername(),
            user.getRole().name()
        );
    }
}