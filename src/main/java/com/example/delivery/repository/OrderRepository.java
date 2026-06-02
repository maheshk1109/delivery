package com.example.delivery.repository;

import com.example.delivery.model.OrderItem;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.List;

public class OrderRepository {

    private final DynamoDbTable<OrderItem> table;
    private final DynamoDbIndex<OrderItem> customerIndex;

    public OrderRepository(DynamoDbEnhancedClient client) {
        table = client.table("orders", TableSchema.fromBean(OrderItem.class));
        customerIndex = table.index("customer-index");
    }

    // ── CREATE / UPDATE ──────────────────────────────────────────────────────
    public void save(OrderItem item) {
        table.putItem(item);
    }

    // ── READ ─────────────────────────────────────────────────────────────────
    public OrderItem findByKey(String orderId, String itemId) {
        return table.getItem(Key.builder()
                .partitionValue(orderId)
                .sortValue(itemId)
                .build());
    }

    // ── UPDATE (optimistic-lock via @DynamoDbVersionAttribute) ───────────────
    public void update(OrderItem item) {
        table.updateItem(r -> r.item(item));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void delete(String orderId, String itemId) {
        table.deleteItem(Key.builder()
                .partitionValue(orderId)
                .sortValue(itemId)
                .build());
    }

    // ── GSI QUERY — all items for a customer ─────────────────────────────────
    public List<OrderItem> findByCustomerId(String customerId) {
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(customerId).build());

        return customerIndex.query(r -> r.queryConditional(condition))
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
