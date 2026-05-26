package com.example.delivery.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String getOrders() {
        return "Orders fetched";
    }
}
