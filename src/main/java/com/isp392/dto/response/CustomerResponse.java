package com.isp392.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties({"accountId"})
public class CustomerResponse extends AccountResponse {
    Integer customerId;
    Integer accountId; // Liên kết với Account
    Double height;
    Double weight;
    Boolean sex;
    Integer portion;
}