package com.isp392.service;

import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.entity.Account;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.AccountMapper;
import com.isp392.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account createAccount(StaffCreationRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Transactional
    public Account createAccount(CustomerCreationRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(Account account, StaffUpdateRequest request) {
        accountMapper.updateAccount(account, request);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(Account account, CustomerUpdateRequest request) {
        accountMapper.updateAccount(account, request);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Integer accountId) {
        accountRepository.deleteById(accountId);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(Integer accountId) {
        return accountRepository.findById(accountId);
    }
}