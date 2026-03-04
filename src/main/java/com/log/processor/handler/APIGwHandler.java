package com.log.processor.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.common.utils.Utils;
import com.log.processor.service.IErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class APIGwHandler implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final IErrorLogService errorLogService;

    @Autowired
    public APIGwHandler(IErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Override
    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent) {
        log.info("In APIGwHandler, received apiGatewayProxyRequestEvent: {}", apiGatewayProxyRequestEvent);
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        List<ErrorLog> errorLogs = errorLogService.loadErrorLogs("Human Intervention Needed", "human-intervention-index");

        if(!errorLogs.isEmpty()) {
            Optional<String> jsonStringOptional = Utils.objectToJsonString(errorLogs);
            if(jsonStringOptional.isPresent()) {
                apiGatewayProxyResponseEvent.setBody(jsonStringOptional.get());
                apiGatewayProxyResponseEvent.setStatusCode(HttpStatus.OK.value());
            } else {
                apiGatewayProxyResponseEvent.setBody("Unexpected Error");
                apiGatewayProxyResponseEvent.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            apiGatewayProxyResponseEvent.setBody("No data found");
            apiGatewayProxyResponseEvent.setStatusCode(HttpStatus.NO_CONTENT.value());
        }

        return apiGatewayProxyResponseEvent;
    }
}
