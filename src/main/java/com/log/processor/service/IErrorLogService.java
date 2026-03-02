package com.log.processor.service;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.entity.ErrorLog;

import java.util.List;

public interface IErrorLogService {

    List<ErrorLog> parse(CloudWatchLogsEvent cloudWatchLogsEvent);

    void updateErrorLog(ErrorLog errorLog);

    ErrorLog loadErrorLog(String eventId, long ingestionTime);

    void saveErrorLogs(List<ErrorLog> errorLogs);
}
