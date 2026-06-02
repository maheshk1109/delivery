package com.example.delivery.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;

@DynamoDbBean
public class OrderItem {

    private String orderId;      // PK
    private String itemId;       // SK
    private String customerId;   // GSI PK  (gsi: customer-index)
    private String status;
    private Integer version;

    @DynamoDbPartitionKey
    public String getOrderId()           { return orderId; }
    public void setOrderId(String v)     { this.orderId = v; }

    @DynamoDbSortKey
    public String getItemId()            { return itemId; }
    public void setItemId(String v)      { this.itemId = v; }

    @DynamoDbSecondaryPartitionKey(indexNames = "customer-index")
    public String getCustomerId()        { return customerId; }
    public void setCustomerId(String v)  { this.customerId = v; }

    public String getStatus()            { return status; }
    public void setStatus(String v)      { this.status = v; }

    @DynamoDbVersionAttribute
    public Integer getVersion()          { return version; }
    public void setVersion(Integer v)    { this.version = v; }
}
