package com.log.processor.common.entity;

import com.log.processor.common.constants.AiAnalysisStatus;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Setter
public class ErrorLog {

    private String eventId;
    private long ingestionTime;
    private String aiAnalysisResponse;

    @Getter
    private String message;

    @Getter
    private AiAnalysisStatus aiAnalysisStatus;

    @DynamoDbSecondaryPartitionKey(indexNames = "human-intervention-index")
    public String getAiAnalysisResponse() {
        return aiAnalysisResponse;
    }

    @DynamoDbPartitionKey
    public String getEventId() {
        return eventId;
    }

    @DynamoDbSortKey
    public long getIngestionTime() {
        return ingestionTime;
    }

    @Override
    public String toString() {
        return "ErrorLogInfo{" +
                "eventId='" + eventId + '\'' +
                ", ingestionTime=" + ingestionTime +
                ", message='" + message + '\'' +
                ", aiAnalysisStatus=" + aiAnalysisStatus +
                ", aiAnalysisResponse='" + aiAnalysisResponse + '\'' +
                '}';
    }
}
