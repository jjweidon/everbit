package com.everbit.everbit.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.everbit.everbit.support.entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
} 