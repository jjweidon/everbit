package com.everbit.everbit.oauth2.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.everbit.everbit.user.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public record CustomOAuth2User(User user) implements OAuth2User {
    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().name();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    public String getId() {
        return user.getId();
    }

}