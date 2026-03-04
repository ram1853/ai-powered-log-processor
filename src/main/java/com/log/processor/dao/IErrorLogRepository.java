package com.log.processor.dao;

import com.log.processor.common.entity.ErrorLog;

import java.util.List;

public interface IErrorLogRepository {

    void saveErrorLogs(List<ErrorLog> errorLog);

    void updateErrorLog(ErrorLog errorLog);

    ErrorLog loadErrorLog(String eventId, long ingestionTime);

    List<ErrorLog> loadErrorLogs(String aiAnalysisResponse, String index);
}
