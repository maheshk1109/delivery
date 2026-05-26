package com.example.delivery.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    @PostMapping
    public String createDelivery() {
        return "Delivery created";
    }

    @GetMapping
    public String getAllDeliveries() {
        return "All deliveries fetched";
    }

    @GetMapping("/{id}")
    public String getDeliveryById(@PathVariable Long id) {
        return "Delivery " + id;
    }

    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id) {
        return "Status updated for delivery " + id;
    }

    @DeleteMapping("/{id}")
    public String cancelDelivery(@PathVariable Long id) {
        return "Delivery cancelled " + id;
    }
}
