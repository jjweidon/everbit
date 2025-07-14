package com.everbit.everbit.upbit.repository;

import com.everbit.everbit.upbit.entity.Account;
import com.everbit.everbit.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUser(User user);
}
