package com.isp392.mapper;

import com.isp392.dto.request.StaffCreationRequest;
import com.isp392.dto.request.CustomerCreationRequest;
import com.isp392.dto.response.AccountResponse;
import com.isp392.entity.Account;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(StaffCreationRequest request);
    Account toAccount(CustomerCreationRequest request);
    AccountResponse toAccountResponse(Account account);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromStaffRequest(@MappingTarget Account account, StaffCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromCustomerRequest(@MappingTarget Account account, CustomerCreationRequest request);
}