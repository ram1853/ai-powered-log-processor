package com.log.processor.handler;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.service.impl.ErrorLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hamcrest.Matchers.*;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CloudWatchHandlerTest {

    @Mock
    private ErrorLogServiceImpl errorLogService;

    @InjectMocks
    private CloudWatchHandler cloudWatchHandler;

    @Test
    void accept_withCloudWatchLogsEvent_shouldReturnParsedErrorLogs() {
        //Arrange
        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();
        ErrorLog errorLog = new ErrorLog();
        errorLog.setEventId("123");
        Mockito.when(errorLogService.parse(cloudWatchLogsEvent)).thenReturn(List.of(errorLog));

        //Act
        List<ErrorLog> errorLogs = cloudWatchHandler.apply(cloudWatchLogsEvent);

        //Assert
        assertThat(errorLogs.get(0).getEventId(), is("123"));
        Mockito.verify(errorLogService, Mockito.times(1)).parse(cloudWatchLogsEvent);
    }
}