package com.log.processor.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.business.AiProcessor;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.handler.APIGwHandler;
import com.log.processor.handler.CloudWatchHandler;
import com.log.processor.service.IErrorLogService;
import com.log.processor.service.IRagAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class AwsEndpoints {

    private final IErrorLogService errorLogService;
    private final IRagAiService ragAiService;

    @Autowired
    public AwsEndpoints(IErrorLogService errorLogService, IRagAiService ragAiService) {
        this.errorLogService = errorLogService;
        this.ragAiService = ragAiService;
    }

    @Bean("cloudWatchHandler")
    public Function<CloudWatchLogsEvent, List<ErrorLog>> cloudWatchHandler(){
        return new CloudWatchHandler(errorLogService);
    }

    @Bean("aiProcessor")
    public Consumer<List<ErrorLog>> aiProcessor() {
        return new AiProcessor(ragAiService, errorLogService);
    }

    @Bean("apiGwHandler")
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> apiGwHandler() {
        return new APIGwHandler(errorLogService);
    }
}
