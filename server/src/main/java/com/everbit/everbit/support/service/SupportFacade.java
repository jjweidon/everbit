package com.everbit.everbit.support.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.support.dto.InquiryRequest;
import com.everbit.everbit.support.entity.Inquiry;
import com.everbit.everbit.support.repository.InquiryRepository;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportFacade {
    private final InquiryRepository inquiryRepository;
    private final UserService userService;

    @Transactional
    public void submitInquiry(String username, InquiryRequest request) {
        User user = userService.findUserByUsername(username);
        Inquiry inquiry = Inquiry.of(request.content(), user);
        inquiryRepository.save(inquiry);
    }
} 