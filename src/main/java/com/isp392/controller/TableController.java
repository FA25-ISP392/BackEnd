package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.TableCreationRequest;
import com.isp392.dto.request.TableUpdateRequest;
import com.isp392.dto.response.TableResponse;
import com.isp392.service.TableService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableController {

    TableService tableService;

    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TableResponse> createTable(@RequestBody TableCreationRequest request) {
        TableResponse response = tableService.createTable(request);
        return ApiResponse.<TableResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<TableResponse>> getAllTables() {
        List<TableResponse> responses = tableService.getAllTables();
        return ApiResponse.<List<TableResponse>>builder()
                .result(responses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TableResponse> getTableById(@PathVariable int id) {
        TableResponse response = tableService.getTableById(id);
        return ApiResponse.<TableResponse>builder()
                .result(response)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<TableResponse> updateTable(@PathVariable int id, @RequestBody TableUpdateRequest request) {
        TableResponse response = tableService.updateTable(id, request);
        return ApiResponse.<TableResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTable(@PathVariable int id) {
        tableService.deleteTable(id);
        return ApiResponse.<Void>builder()
                .result(null)
                .build();
    }
}
