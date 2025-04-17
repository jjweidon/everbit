package com.everbit.everbit.upbit.repository;

import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.upbit.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByMember(Member member);
}
