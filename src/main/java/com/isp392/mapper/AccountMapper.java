package com.isp392.mapper;

import com.isp392.dto.request.*;
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
    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);


}