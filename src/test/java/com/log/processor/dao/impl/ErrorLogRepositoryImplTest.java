package com.log.processor.dao.impl;

import com.log.processor.common.constants.AiAnalysisStatus;
import com.log.processor.common.constants.Constants;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.common.exception.DataLakeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ErrorLogRepositoryImplTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @InjectMocks
    private ErrorLogRepositoryImpl errorLogRepository;

    @Test
    void putRecords_withErrorLogs_shouldBatchWriteToDynamo() {
        // Arrange
        ErrorLog errorLog = new ErrorLog();
        errorLog.setEventId("1");
        errorLog.setIngestionTime(12345);
        errorLog.setMessage("Log Level: Error | Message: WriteProvisionedThroughputExceeded");
        errorLog.setAiAnalysisStatus(AiAnalysisStatus.Pending);

        //Returns_Deep_Stubs mocks the all method calls that happen internally.
        DynamoDbTable<ErrorLog> dynamoDbTable =
                Mockito.mock(DynamoDbTable.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(dynamoDbEnhancedClient.table(
                        Mockito.eq(Constants.ERROR_LOG_TABLE),
                        Mockito.any(TableSchema.class)))
                .thenReturn(dynamoDbTable);

        // Act
        errorLogRepository.saveErrorLogs(List.of(errorLog));

        // Assert
        Mockito.verify(dynamoDbEnhancedClient, Mockito.times(1))
                .batchWriteItem(Mockito.any(BatchWriteItemEnhancedRequest.class));
    }

    @Test
    void putRecords_withFailedBatchWrite_shouldThrowDataLakeException() {
        // Arrange
        ErrorLog errorLog = new ErrorLog();
        errorLog.setEventId("1");
        errorLog.setIngestionTime(12345);
        errorLog.setMessage("Log Level: Error | Message: WriteProvisionedThroughputExceeded");
        errorLog.setAiAnalysisStatus(AiAnalysisStatus.Pending);

        Mockito.when(dynamoDbEnhancedClient.table(
                        Mockito.eq(Constants.ERROR_LOG_TABLE),
                        Mockito.any(TableSchema.class)))
                .thenThrow(DynamoDbException.builder().message("schema is null").build());

        // Act and Assert
        assertThrows(DataLakeException.class, () -> errorLogRepository.saveErrorLogs(List.of(errorLog)));
    }

    static List<List<ErrorLog>> inputs() {
        return Arrays.asList(new ArrayList<>(), null);
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void putRecords_withNoRecords_shouldNotPersistAnything(List<ErrorLog> errorLogs) {
        // Act
        errorLogRepository.saveErrorLogs(errorLogs);

        // Assert
        Mockito.verify(dynamoDbEnhancedClient, Mockito.never()).batchWriteItem(Mockito.any(BatchWriteItemEnhancedRequest.class));
    }
}