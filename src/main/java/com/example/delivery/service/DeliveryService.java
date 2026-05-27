package com.example.delivery.service;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    public String createDelivery() {
        return "Delivery created";
    }

    public String getAllDeliveries() {
        return "All deliveries fetched";
    }

    public String getDeliveryById(Long id) {
        return "Delivery " + id;
    }

    public String updateStatus(Long id) {
        return "Status updated for delivery " + id;
    }

    public String cancelDelivery(Long id) {
        return "Delivery cancelled " + id;
    }
}
