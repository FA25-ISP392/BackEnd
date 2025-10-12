package com.isp392.mapper;

import com.isp392.dto.request.TableCreationRequest;
import com.isp392.dto.request.TableUpdateRequest;
import com.isp392.dto.response.TableResponse;
import com.isp392.entity.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TableMapper {

    TableEntity toEntity(TableCreationRequest request);

    TableResponse toResponse(TableEntity entity);

    void updateTable(@MappingTarget TableEntity entity, TableUpdateRequest request);
}
