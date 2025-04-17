package com.everbit.everbit.oauth2.dto;

import com.everbit.everbit.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final MemberDto memberDto;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDto.role();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return memberDto.username();
    }

    public String getId() {
        return memberDto.memberId();
    }
}