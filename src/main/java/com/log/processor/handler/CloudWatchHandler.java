package com.log.processor.handler;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.service.IErrorLogService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class CloudWatchHandler implements Function<CloudWatchLogsEvent, List<ErrorLog>> {

    private final IErrorLogService errorLogService;

    public CloudWatchHandler(IErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Override
    public List<ErrorLog> apply(CloudWatchLogsEvent cloudWatchLogsEvent) {
        try {
            log.info("In CloudWatchHandler, got cloudWatchLogsEvent: {}", cloudWatchLogsEvent);
            return errorLogService.parse(cloudWatchLogsEvent);
        } catch (Exception e) {
            log.error("Parsing of CloudWatchLogsEvent failed", e);
            //TODO: Update CloudWatch Metric (will help in monitoring the Lambda errors)
            return Collections.emptyList();
        }
    }
}
