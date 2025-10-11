package com.isp392.service;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Customer;
import com.isp392.entity.Staff;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.CustomerMapper;
import com.isp392.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Transactional
    public CustomerResponse createCustomer(@Valid CustomerCreationRequest request) {
        Account account = accountService.createAccount(request);
        Customer customer = customerMapper.toCustomer(request);
        customer.setAccount(account);
        customerRepository.save(customer);
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