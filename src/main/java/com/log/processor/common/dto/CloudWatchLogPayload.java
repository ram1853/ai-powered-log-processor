package com.log.processor.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CloudWatchLogPayload {

    private String messageType;
    private String owner;
    private String logGroup;
    private String logStream;
    private List<String> subscriptionFilters;
    private List<LogEvent> logEvents;

    @Getter
    @Setter
    public static class LogEvent {

        private String id;
        private long timestamp;
        private String message;
    }
}
