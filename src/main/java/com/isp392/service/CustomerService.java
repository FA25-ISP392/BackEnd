package com.isp392.service;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.request.StaffUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.dto.response.StaffResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Customer;
import com.isp392.entity.Staff;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.CustomerMapper;
import com.isp392.repository.AccountRepository;
import com.isp392.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    AccountService accountService;
    private final AccountRepository accountRepository;

    @Transactional
    public CustomerResponse createCustomer(@Valid CustomerCreationRequest request) {
        Account account = accountService.createAccount(request);
        Customer customer = customerMapper.toCustomer(request);
        customer.setAccount(account);
        customerRepository.save(customer);
        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomer() {
        List<Customer> staffList = customerRepository.findAll();
        return customerMapper.toCustomerResponseList(staffList);
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
        customerMapper.updateCustomer(customer, request);

        Account account = customer.getAccount();
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        accountRepository.save(account);
        customer = customerRepository.save(customer);

        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        Account account = customer.getAccount();
        customerRepository.delete(customer);
        accountRepository.delete(account);
    }
}