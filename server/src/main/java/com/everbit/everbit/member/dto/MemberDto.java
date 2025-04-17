package com.everbit.everbit.member.dto;

import com.everbit.everbit.member.entity.Member;

public record MemberDto(
    String username,
    String role
) {
    public static MemberDto from(Member member) {
        return new MemberDto(
            member.getUsername(),
            member.getRole().name()
        );
    }
}