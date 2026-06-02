package com.example.delivery.stream;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingRequest;
import software.amazon.awssdk.services.lambda.model.EventSourcePosition;

/**
 * Run once to:
 *  1. Enable DynamoDB stream (NEW_AND_OLD_IMAGES) on the orders table
 *  2. Create Lambda event source mapping pointing at the stream
 */
public class StreamSetup {

    static final String TABLE      = "orders";
    static final String LAMBDA_ARN = System.getenv("ORDER_STREAM_LAMBDA_ARN");
    static final Region REGION     = Region.AP_SOUTHEAST_2;

    public static void main(String[] args) {
        try (DynamoDbClient dynamo = DynamoDbClient.builder().region(REGION).build();
             LambdaClient lambda  = LambdaClient.builder().region(REGION).build()) {

            String streamArn = enableStream(dynamo);
            System.out.println("Stream ARN: " + streamArn);

            wireEventSourceMapping(lambda, streamArn);
            System.out.println("Event source mapping created.");
        }
    }

    static String enableStream(DynamoDbClient dynamo) {
        dynamo.updateTable(r -> r
                .tableName(TABLE)
                .streamSpecification(StreamSpecification.builder()
                        .streamEnabled(true)
                        .streamViewType(StreamViewType.NEW_AND_OLD_IMAGES)
                        .build()));

        return dynamo.describeTable(r -> r.tableName(TABLE))
                .table()
                .latestStreamArn();
    }

    static void wireEventSourceMapping(LambdaClient lambda, String streamArn) {
        lambda.createEventSourceMapping(CreateEventSourceMappingRequest.builder()
                .functionName(LAMBDA_ARN)
                .eventSourceArn(streamArn)
                .startingPosition(EventSourcePosition.TRIM_HORIZON)
                .batchSize(10)
                .bisectBatchOnFunctionError(true)   // retry only failed half on error
                .build());
    }
}
