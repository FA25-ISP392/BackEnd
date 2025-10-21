package com.isp392.service;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Customer;
import com.isp392.entity.EmailVerificationToken;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.CustomerMapper;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.EmailVerificationTokenRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    AccountService accountService;
    EmailVerificationTokenRepository emailTokenRepository;
    EmailService emailService;
    @NonFinal
    @Value("${app.frontend.verify-email-url:http://localhost:5173/verify-email}") // Thêm config này ở Bước 4
    String frontendVerifyEmailUrl;

    @Transactional
    public CustomerResponse createCustomer(@Valid CustomerCreationRequest request) {
        Account account = accountService.createAccount(request);
        Customer customer = customerMapper.toCustomer(request);
        customer.setAccount(account);
        customerRepository.save(customer);
        emailTokenRepository.deleteByEmail(account.getEmail());
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .email(account.getEmail())
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        emailTokenRepository.save(verificationToken);
        // Tạo link xác thực
        String verifyLink = frontendVerifyEmailUrl + "?token=" + token;

        // Gửi email (Bạn cần thêm method này ở Bước 5)
        emailService.sendVerificationEmail(account.getEmail(), account.getFullName(), verifyLink);

        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomer(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("customerId").ascending());
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerPage.stream().map(customerMapper::toCustomerResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Integer customerId, String usernameJwt) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (!customer.getAccount().getUsername().equals(usernameJwt)) {
            throw new AppException(ErrorCode.STAFF_ACCESS_FORBIDDEN);
        }
        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // Update các field của Customer (trừ Account)
        customerMapper.updateCustomer(customer, request);

        // Update Account riêng
        Account account = customer.getAccount();
        accountService.updateAccount(account, request);

        return customerMapper.toCustomerResponse(customer);
    }


    @Transactional
    public void deleteCustomer(Integer customerId) {
        customerRepository.deleteById(customerId);
    }
}