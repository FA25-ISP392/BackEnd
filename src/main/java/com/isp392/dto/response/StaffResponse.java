package com.isp392.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonIgnoreProperties({"accountId"})
public class StaffResponse extends AccountResponse {
    Integer staffId;
    Integer accountId; // Liên kết với Account
}
