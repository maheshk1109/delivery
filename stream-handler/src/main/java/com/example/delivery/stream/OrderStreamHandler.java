package com.example.delivery.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class OrderStreamHandler implements RequestHandler<DynamodbEvent, Void> {

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbStreamRecord record : event.getRecords()) {
            String eventName = record.getEventName();
            var dynamodb = record.getDynamodb();

            switch (eventName) {
                case "INSERT" -> {
                    var newItem = dynamodb.getNewImage();
                    context.getLogger().log("INSERT — orderId: " + newItem.get("orderId").getS()
                            + " itemId: " + newItem.get("itemId").getS()
                            + " status: " + newItem.get("status").getS());
                }
                case "MODIFY" -> {
                    var oldItem = dynamodb.getOldImage();
                    var newItem = dynamodb.getNewImage();
                    context.getLogger().log("MODIFY — orderId: " + newItem.get("orderId").getS()
                            + " status: " + oldItem.get("status").getS()
                            + " -> " + newItem.get("status").getS());
                }
                case "REMOVE" -> {
                    var oldItem = dynamodb.getOldImage();
                    context.getLogger().log("REMOVE — orderId: " + oldItem.get("orderId").getS()
                            + " itemId: " + oldItem.get("itemId").getS());
                }
            }
        }
        return null;
    }
}
