package com.isp392.service;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Account;
import com.isp392.entity.Customer;
import com.isp392.entity.Staff;
import com.isp392.enums.Role;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.CustomerMapper;
import com.isp392.repository.AccountRepository;
import com.isp392.repository.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    AccountRepository accountRepository;
    CustomerMapper customerMapper;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Transactional
    public Customer createCustomer(CustomerCreationRequest request) {
        // Kiểm tra username đã tồn tại trong Account chưa
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 🔹 Tạo Account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        accountRepository.save(account);

        // 🔹 Tạo Cus và gán Account
        Customer customer = customerMapper.toCustomer(request);
        customer.setAccount(account);

        return customerRepository.save(customer);
    }

    public List<CustomerResponse> findByName(String name) {
        List<Customer> customers = customerRepository.findByCustomerNameContainingIgnoreCase(name);
        return customerMapper.toCustomerResponseList(customers);
    }

    public List<CustomerResponse> findAll() {
        return customerMapper.toCustomerResponseList(customerRepository.findAll());
    }

    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        customerMapper.updateCustomer(customer, request);
        return customerMapper.toCustomerResponse(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
