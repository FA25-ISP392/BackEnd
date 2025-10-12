package com.isp392.controller;

import com.isp392.dto.request.*;
import com.isp392.dto.response.AccountResponse;
import com.isp392.dto.response.ApiResponse;
import com.isp392.entity.Account;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {

    AccountService accountService;

    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody Object request) {
        Account account;
        if (request instanceof StaffCreationRequest) {
            account = accountService.createAccount((StaffCreationRequest) request);
        } else if (request instanceof CustomerCreationRequest) {
            account = accountService.createAccount((CustomerCreationRequest) request);
        } else {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        return ApiResponse.<AccountResponse>builder()
                .result(new AccountResponse(account))
                .build();
    }

    @PutMapping("/{accountId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccountResponse> updateAccount(@PathVariable Integer accountId, @Valid @RequestBody Object request) {
        Account account = accountService.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request instanceof StaffUpdateRequest) {
            account = accountService.updateAccount(account, (StaffUpdateRequest) request);
        } else if (request instanceof CustomerUpdateRequest) {
            account = accountService.updateAccount(account, (CustomerUpdateRequest) request);
        } else {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }

        return ApiResponse.<AccountResponse>builder()
                .result(new AccountResponse(account))
                .build();
    }

    @DeleteMapping("/{accountId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAccount(@PathVariable Integer accountId) {
        accountService.deleteAccount(accountId);
        return ApiResponse.<Void>builder()
                .message("Account deleted successfully")
                .build();
    }
}
