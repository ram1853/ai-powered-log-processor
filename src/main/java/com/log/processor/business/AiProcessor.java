package com.log.processor.business;

import com.log.processor.common.constants.AiAnalysisStatus;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.service.IErrorLogService;
import com.log.processor.service.IRagAiService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class AiProcessor implements Consumer<List<ErrorLog>> {

    private final IRagAiService ragAiService;
    private final IErrorLogService errorLogService;

    public AiProcessor(IRagAiService ragAiService, IErrorLogService errorLogService) {
        this.ragAiService = ragAiService;
        this.errorLogService = errorLogService;
    }

    @Override
    public void accept(List<ErrorLog> errorLogs) {
        log.info("In AiProcessor, received errorLogs: {}", errorLogs);
        if(errorLogs.isEmpty()) return;
        List<ErrorLog> updatedErrorLogs = new ArrayList<>();
        errorLogs.forEach((ErrorLog errorLog) -> {
            String aiResponse = ragAiService.queryKnowledgeBase(errorLog.getMessage());
            errorLog.setAiAnalysisStatus(AiAnalysisStatus.Completed);
            errorLog.setAiAnalysisResponse(aiResponse);
            updatedErrorLogs.add(errorLog);
        });
        errorLogService.saveErrorLogs(updatedErrorLogs);
    }
}
