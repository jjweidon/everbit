package com.everbit.everbit.oauth2.dto;

import com.everbit.everbit.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public record CustomOAuth2User(Member member) implements OAuth2User {
    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", member.getId(),
                "username", member.getUsername(),
                "role", member.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return member.getRole().name();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return member.getUsername();
    }

    public String getId() {
        return member.getId();
    }

}