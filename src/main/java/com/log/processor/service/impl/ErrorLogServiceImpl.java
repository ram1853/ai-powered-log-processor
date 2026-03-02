package com.log.processor.service.impl;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.constants.AiAnalysisStatus;
import com.log.processor.common.dto.CloudWatchLogPayload;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.common.utils.Utils;
import com.log.processor.dao.IErrorLogRepository;
import com.log.processor.service.IErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ErrorLogServiceImpl implements IErrorLogService {

    private final IErrorLogRepository errorLogRepository;

    @Autowired
    public ErrorLogServiceImpl(IErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Override
    public List<ErrorLog> parse(CloudWatchLogsEvent cloudWatchLogsEvent) {
        List<ErrorLog> errorLogs = new ArrayList<>();
        Utils.decompressCloudWatchLogsEvent(cloudWatchLogsEvent).ifPresent((String cloudWatchLogPayload) -> {
            log.info("cloudWatchLogPayload: {}", cloudWatchLogPayload);
            Utils.jsonStringToObject(cloudWatchLogPayload, CloudWatchLogPayload.class)
                    .ifPresent((CloudWatchLogPayload payload) -> {
                        payload.getLogEvents().forEach((CloudWatchLogPayload.LogEvent logEvent) -> {
                            ErrorLog errorLog = new ErrorLog();
                            errorLog.setEventId(logEvent.getId());
                            errorLog.setIngestionTime(logEvent.getTimestamp());
                            errorLog.setMessage(logEvent.getMessage());
                            errorLog.setAiAnalysisStatus(AiAnalysisStatus.Pending);
                            errorLogs.add(errorLog);
                        });
                    });
        });
        return errorLogs;
    }

    @Override
    public void updateErrorLog(ErrorLog errorLog) {
        errorLogRepository.updateErrorLog(errorLog);
    }

    @Override
    public ErrorLog loadErrorLog(String eventId, long ingestionTime) {
        return errorLogRepository.loadErrorLog(eventId, ingestionTime);
    }

    @Override
    public void saveErrorLogs(List<ErrorLog> errorLogs) {
        errorLogRepository.saveErrorLogs(errorLogs);
    }
}
