package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;

import com.everbit.everbit.upbit.exception.AccountException;
import com.everbit.everbit.upbit.repository.AccountRepository;
import com.everbit.everbit.upbit.entity.Account;
import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account findAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .orElseThrow(() -> AccountException.noAccount(user));
    }

    public Account findOrCreateAccount(User user) {
        return accountRepository.findByUser(user)
                .orElseGet(() -> Account.init(user));
    }

    public boolean isAccountExists(User user) {
        return accountRepository.findByUser(user).isPresent();
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

}
