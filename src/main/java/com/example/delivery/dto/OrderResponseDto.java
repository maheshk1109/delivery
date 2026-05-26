package com.example.delivery.dto;

import lombok.Data;

@Data
public class OrderResponseDto {
    private Long id;
    private String itemName;
    private Integer quantity;
}
