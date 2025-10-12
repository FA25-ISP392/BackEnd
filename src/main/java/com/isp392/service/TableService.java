package com.isp392.service;

import com.isp392.dto.request.TableCreationRequest;
import com.isp392.dto.request.TableUpdateRequest;
import com.isp392.dto.response.TableResponse;
import com.isp392.entity.TableEntity;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.TableMapper;
import com.isp392.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    public TableResponse createTable(TableCreationRequest request) {
        TableEntity table = tableMapper.toEntity(request);
        return tableMapper.toResponse(tableRepository.save(table));
    }

    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(tableMapper::toResponse)
                .toList();
    }

    public TableResponse getTableById(int id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ARGUMENT));
        return tableMapper.toResponse(table);
    }

    public TableResponse updateTable(int id, TableUpdateRequest request) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ARGUMENT));
        tableMapper.updateTable(table, request);
        return tableMapper.toResponse(tableRepository.save(table));
    }

    public void deleteTable(int id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ARGUMENT));
        tableRepository.delete(table);
    }
}
