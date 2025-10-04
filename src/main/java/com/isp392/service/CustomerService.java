package com.isp392.service;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Customer;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.CustomerMapper;
import com.isp392.repository.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {

    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

    public CustomerResponse findByName(String name) {
        return customerMapper.toCustomerResponse(customerRepository.findByFullNameContainingIgnoreCase(name));
    }
    public List<CustomerResponse> findAll() {
        return customerMapper.toCustomerResponseList(customerRepository.findAll());
    }
    public CustomerResponse createCustomer(CustomerCreationRequest request) {
        Customer customer = customerMapper.toCustomer(request);
        return customerMapper.toCustomerResponse(customerRepository.save(customer));
    }
    public CustomerResponse updateCustomer(long id, CustomerUpdateRequest request) {
        Customer customer= customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        customerMapper.updateCustomer(customer, request);
        return customerMapper.toCustomerResponse(customerRepository.save(customer));
    }
    public void deleteCustomer(long id) {
        customerRepository.deleteById(id);
    }
}
