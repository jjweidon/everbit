package com.everbit.everbit.dto;

import com.everbit.everbit.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    private String role;
    private String username;

    public static MemberDto from(Member member) {
        return MemberDto.builder()
                .role(member.getRole().name())
                .username(member.getUsername())
                .build();
    }
}