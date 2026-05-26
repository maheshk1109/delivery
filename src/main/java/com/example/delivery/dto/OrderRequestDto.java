package com.example.delivery.dto;

import lombok.Data;

@Data
public class OrderRequestDto {
    private String itemName;
    private Integer quantity;
}
