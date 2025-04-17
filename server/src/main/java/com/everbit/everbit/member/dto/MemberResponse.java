package com.everbit.everbit.member.dto;

import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.member.entity.Member;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MemberResponse(
    String memberId,
    String username,
    String nickname,
    String image,
    Boolean isUpbitConnected
) implements Response {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getImage(),
            member.isUpbitConnected()
        );
    }
}