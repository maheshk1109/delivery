package com.example.delivery.repository;

import com.example.delivery.model.OrderItem;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderRepositoryTest {

    static DynamoDbClient dynamo;
    static OrderRepository repo;

    @BeforeAll
    static void setup() {
        dynamo = DynamoDbClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .build();

        createTable();

        repo = new OrderRepository(
                DynamoDbEnhancedClient.builder().dynamoDbClient(dynamo).build());
    }

    @AfterAll
    static void teardown() {
        dynamo.deleteTable(r -> r.tableName("orders"));
        dynamo.close();
    }

    static void createTable() {
        dynamo.createTable(r -> r
                .tableName("orders")
                .keySchema(
                        KeySchemaElement.builder().attributeName("orderId").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("itemId").keyType(KeyType.RANGE).build())
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("orderId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("itemId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("customerId").attributeType(ScalarAttributeType.S).build())
                .globalSecondaryIndexes(GlobalSecondaryIndex.builder()
                        .indexName("customer-index")
                        .keySchema(KeySchemaElement.builder().attributeName("customerId").keyType(KeyType.HASH).build())
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));

        // wait until table is ACTIVE
        dynamo.waiter().waitUntilTableExists(r -> r.tableName("orders"));
    }

    static OrderItem item(String orderId, String itemId, String customerId) {
        OrderItem o = new OrderItem();
        o.setOrderId(orderId);
        o.setItemId(itemId);
        o.setCustomerId(customerId);
        o.setStatus("PENDING");
        return o;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Test
    void save_and_findByKey() {
        repo.save(item("o1", "i1", "c1"));

        OrderItem found = repo.findByKey("o1", "i1");
        assertThat(found).isNotNull();
        assertThat(found.getStatus()).isEqualTo("PENDING");
        assertThat(found.getVersion()).isEqualTo(1);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Test
    void update_incrementsVersion() {
        repo.save(item("o2", "i1", "c2"));
        OrderItem saved = repo.findByKey("o2", "i1");

        saved.setStatus("SHIPPED");
        repo.update(saved);

        OrderItem updated = repo.findByKey("o2", "i1");
        assertThat(updated.getStatus()).isEqualTo("SHIPPED");
        assertThat(updated.getVersion()).isEqualTo(2);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Test
    void delete_removesItem() {
        repo.save(item("o3", "i1", "c3"));
        repo.delete("o3", "i1");

        assertThat(repo.findByKey("o3", "i1")).isNull();
    }

    // ── GSI QUERY ─────────────────────────────────────────────────────────────
    @Test
    void gsiQuery_returnsByCustomerId() {
        repo.save(item("o4", "i1", "c4"));
        repo.save(item("o4", "i2", "c4"));
        repo.save(item("o5", "i1", "c5"));

        List<OrderItem> results = repo.findByCustomerId("c4");
        assertThat(results).hasSize(2)
                .allMatch(oi -> "c4".equals(oi.getCustomerId()));
    }

    // ── CONCURRENT VERSION CONFLICT ───────────────────────────────────────────
    @Test
    void concurrentUpdate_throwsOnStaleVersion() {
        repo.save(item("o6", "i1", "c6"));

        OrderItem staleRead1 = repo.findByKey("o6", "i1");  // version=1
        OrderItem staleRead2 = repo.findByKey("o6", "i1");  // version=1

        staleRead1.setStatus("SHIPPED");
        repo.update(staleRead1);   // succeeds → version becomes 2

        staleRead2.setStatus("CANCELLED");
        assertThatThrownBy(() -> repo.update(staleRead2))
                .isInstanceOf(software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException.class);
    }
}
