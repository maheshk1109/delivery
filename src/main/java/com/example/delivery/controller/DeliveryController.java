package com.example.delivery.controller;

import com.example.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public String createDelivery() {
        return deliveryService.createDelivery();
    }

    @GetMapping
    public String getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @GetMapping("/{id}")
    public String getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id);
    }

    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id) {
        return deliveryService.updateStatus(id);
    }

    @DeleteMapping("/{id}")
    public String cancelDelivery(@PathVariable Long id) {
        return deliveryService.cancelDelivery(id);
    }
}
