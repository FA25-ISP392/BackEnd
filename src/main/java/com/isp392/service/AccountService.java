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
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    public Account createAccount(StaffCreationRequest request) {
        checkAccountUniqueness(request.getUsername(), request.getEmail(), request.getPhone());

        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Transactional
    public Account createAccount(CustomerCreationRequest request) {
        checkAccountUniqueness(request.getUsername(), request.getEmail(), request.getPhone());

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

    //Helpter method
    private void checkAccountUniqueness(String username, String email, String phone) {
        boolean usernameExists = accountRepository.existsByUsername(username);
        boolean emailExists = false;
        boolean phoneExists = false;

        if (email != null && !email.isEmpty()) {
            emailExists = accountRepository.findByEmail(email).isPresent();
        }

        if (phone != null && !phone.isEmpty()) {
            phoneExists = accountRepository.existsByPhone(phone);
        }

        // 2. Ném lỗi theo thứ tự ưu tiên
        if (emailExists && phoneExists) {
            throw new AppException(ErrorCode.EMAIL_AND_PHONE_EXISTED);
        }
        if (emailExists) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (phoneExists) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        if (usernameExists) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }
}