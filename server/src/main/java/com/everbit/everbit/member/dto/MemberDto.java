package com.everbit.everbit.member.dto;

import com.everbit.everbit.member.entity.Member;

public record MemberDto(
    String memberId,
    String role,
    String username
) {
    public static MemberDto from(Member member) {
        return new MemberDto(
            member.getId(),
            member.getRole().name(),
            member.getUsername()
        );
    }
}