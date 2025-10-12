package com.isp392.controller;

import com.isp392.dto.request.TableCreationRequest;
import com.isp392.dto.request.TableUpdateRequest;
import com.isp392.dto.response.TableResponse;
import com.isp392.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@RequestBody TableCreationRequest request) {
        return ResponseEntity.ok(tableService.createTable(request));
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable int id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableResponse> updateTable(@PathVariable int id,
                                                     @RequestBody TableUpdateRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable int id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
