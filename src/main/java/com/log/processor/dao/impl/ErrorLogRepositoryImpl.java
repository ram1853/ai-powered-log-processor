package com.log.processor.dao.impl;

import com.log.processor.common.constants.Constants;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.common.exception.DataLakeException;
import com.log.processor.dao.IErrorLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ErrorLogRepositoryImpl implements IErrorLogRepository {

    private static final String UPDATE_ITEM_EXPRESSION = "#PK = :eventId AND #SK = :ingestionTime";

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Autowired
    public ErrorLogRepositoryImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    }

    @Override
    public void saveErrorLogs(List<ErrorLog> errorLogs) {
        try {
            if(errorLogs == null || errorLogs.isEmpty()) return;
            DynamoDbTable<ErrorLog> errorLogTable = dynamoDbEnhancedClient
                    .table(Constants.ERROR_LOG_TABLE, TableSchema.fromBean(ErrorLog.class));
            WriteBatch.Builder<ErrorLog> writeBatchBuilder = WriteBatch.builder(ErrorLog.class)
                            .mappedTableResource(errorLogTable);
            for (ErrorLog errorLog: errorLogs) {
                writeBatchBuilder.addPutItem(r -> r.item(errorLog));
            }
            BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest
                    .builder()
                    .writeBatches(writeBatchBuilder.build())
                    .build();
            dynamoDbEnhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
            log.info("Error logs added to dynamo: {}", errorLogs);
        } catch (DynamoDbException e) {
            log.error("Failed to add Error logs to dynamo", e);
            throw new DataLakeException("Failed to add Error logs to dynamo: "+errorLogs);
        }
    }

    @Override
    public void updateErrorLog(ErrorLog errorLog) {
        try {
            DynamoDbTable<ErrorLog> errorLogTable = dynamoDbEnhancedClient
                    .table(Constants.ERROR_LOG_TABLE, TableSchema.fromBean(ErrorLog.class));

            Map<String, String> expressionNames = new HashMap<>();
            expressionNames.put("#PK", "eventId");
            expressionNames.put("#SK", "ingestionTime");

            Map<String, AttributeValue> expressionValues = new HashMap<>();
            expressionValues.put(":eventId", AttributeValue.builder().s(errorLog.getEventId()).build());
            expressionValues.put(":ingestionTime", AttributeValue.builder().n(String.valueOf(errorLog.getIngestionTime())).build());

            Expression itemExistsExpression = Expression.builder()
                    .expression(UPDATE_ITEM_EXPRESSION)
                    .expressionNames(expressionNames)
                    .expressionValues(expressionValues)
                    .build();

            UpdateItemEnhancedRequest<ErrorLog> updateRequest =
                    UpdateItemEnhancedRequest.builder(ErrorLog.class)
                            .item(errorLog)
                            .conditionExpression(itemExistsExpression)
                            .ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY)
                            .build();
            errorLogTable.updateItem(updateRequest);
        } catch (DynamoDbException e) {
            log.error("Failed to update Error log to dynamo", e);
            throw new DataLakeException("Failed to update Error log to dynamo: "+errorLog);
        }
    }

    @Override
    public ErrorLog loadErrorLog(String eventId, long ingestionTime) {
        try {
            DynamoDbTable<ErrorLog> errorLogTable = dynamoDbEnhancedClient
                    .table(Constants.ERROR_LOG_TABLE, TableSchema.fromBean(ErrorLog.class));
            return errorLogTable.getItem(Key.builder().partitionValue(eventId).sortValue(ingestionTime).build());
        } catch (DynamoDbException e) {
            log.error("Failed to fetch Error log from dynamo", e);
            throw new DataLakeException("Failed to fetch Error log from dynamo: "+eventId+","+ingestionTime);
        }
    }

    @Override
    public List<ErrorLog> loadErrorLogs(String aiAnalysisResponse, String index) {
        try {
            DynamoDbTable<ErrorLog> errorLogTable = dynamoDbEnhancedClient
                    .table(Constants.ERROR_LOG_TABLE, TableSchema.fromBean(ErrorLog.class));
            DynamoDbIndex<ErrorLog> errorLogDynamoDbIndex = errorLogTable.index(index);

            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(aiAnalysisResponse).build());
            QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                    .queryConditional(queryConditional)
                    .build();
            SdkIterable<Page<ErrorLog>> sdkIterable = errorLogDynamoDbIndex.query(queryEnhancedRequest);
            PageIterable<ErrorLog> pageIterable = PageIterable.create(sdkIterable);
            return pageIterable.items().stream().collect(Collectors.toList());
        } catch (DynamoDbException e) {
            log.error("Failed to retrieve error logs from index", e);
            throw new DataLakeException("Failed to fetch Error logs from dynamo. Index: "+index+", aiAnalysisResponse: "+aiAnalysisResponse);
        }
    }
}
