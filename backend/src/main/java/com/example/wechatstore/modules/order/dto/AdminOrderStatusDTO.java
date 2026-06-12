package com.example.wechatstore.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminOrderStatusDTO {

    @NotBlank(message = "status is required")
    private String status;
}
