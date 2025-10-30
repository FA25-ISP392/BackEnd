package com.isp392.service;

import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.entity.Account;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.exception.MultipleFieldsException;
import com.isp392.mapper.AccountMapper;
import com.isp392.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
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
        // 1. Chuẩn bị một map để chứa các lỗi
        Map<String, ErrorCode> errors = new HashMap<>();
        // 2. Kiểm tra Username
        if (username != null && !username.isEmpty()) {
            if (accountRepository.existsByUsername(username)) {
                errors.put("username", ErrorCode.USER_EXISTED);
            }
        }
        // 3. Kiểm tra Email
        if (email != null && !email.isEmpty()) {
            if (accountRepository.findByEmail(email).isPresent()) {
                errors.put("email", ErrorCode.EMAIL_EXISTED);
            }
        }
        // 4. Kiểm tra Phone
        if (phone != null && !phone.isEmpty()) {
            if (accountRepository.existsByPhone(phone)) {
                errors.put("phone", ErrorCode.PHONE_EXISTED);
            }
        }
        // 5. Nếu có bất kỳ lỗi nào trong map, ném Exception mới
        if (!errors.isEmpty()) {
            throw new MultipleFieldsException(errors, ErrorCode.INVALID_ARGUMENT);
        }
    }
}