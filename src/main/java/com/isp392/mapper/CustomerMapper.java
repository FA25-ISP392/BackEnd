package com.isp392.mapper;

import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.request.CustomerUpdateRequest;
import com.isp392.dto.response.CustomerResponse;
import com.isp392.entity.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = AccountMapper.class)
public interface CustomerMapper {

    Customer toCustomer(CustomerCreationRequest customer);

    @Mapping(target = ".", source = "account")
    CustomerResponse toCustomerResponse(Customer customer);

    List<CustomerResponse> toCustomerResponseList(List<Customer> customers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomer(@MappingTarget Customer customer, CustomerUpdateRequest request);
}
