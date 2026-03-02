package com.log.processor.service.impl;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.dao.IErrorLogRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ErrorLogServiceImplTest {

    @Mock
    private IErrorLogRepository errorLogRepository;

    @InjectMocks
    private ErrorLogServiceImpl errorLogService;

    @Test
    void saveErrorLogs_shouldCallRepositoryForSavingErrorLogs() {
        //Arrange
        List<ErrorLog> list = new ArrayList<>();
        ErrorLog errorLog = new ErrorLog();
        errorLog.setEventId("123");
        list.add(errorLog);

        //Act
        errorLogService.saveErrorLogs(list);

        //Assert
        Mockito.verify(errorLogRepository, Mockito.times(1)).saveErrorLogs(list);
    }

    @Test
    void parse_withCloudWatchLogsEvent_shouldParseAndReturnErrorLogs() {
        //Arrange
        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();
        CloudWatchLogsEvent.AWSLogs awsLogs = new CloudWatchLogsEvent.AWSLogs();
        awsLogs.withData("H4sIAAAAAAAA/02Py2rCQBSGX2U46xTmnszshKZuKhQidCFS1Bx1qMmEM5O0xfrupYZCd/+Fb/FdocOUdidcfw0IHh4X68Xbqm6axbKGAuJHjwQejDacu8raUhgo4BJPS4rjAP43PuCEfU7z3mTCXQce0j0IKCCN+3SgMOQQ+6dwyUgJ/OY/ub2j9Vz85gqhBQ/KGS6lk7wyRnIlOXel5K4SXGkpK+F0xZ1V0hrpRKmctdJxpzUUkEOHKe+6AbwoS6G05sqZ0hZ/uuDhGSe8eFYTRWLfbDUfnr1SyPhCcQopxB7b9ZnieDoPY64/D4gttuwYic2inu0pHN4T3La3H4Eh/1xMAQAA");
        cloudWatchLogsEvent.setAwsLogs(awsLogs);

        //Act
        List<ErrorLog> errorLogs = errorLogService.parse(cloudWatchLogsEvent);

        //Assert
        assertThat(errorLogs, is(Matchers.not(empty())));
    }

    @Test
    void parse_withNoCloudWatchLogsEvent_shouldNotParseAnything() {
        //Arrange
        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();

        //Act
        List<ErrorLog> errorLogs = errorLogService.parse(cloudWatchLogsEvent);

        //Assert
        assertThat(errorLogs, is(empty()));
    }

    @Test
    void parse_withIllegalEncodedCharacterInCloudWatchLogsEvent_shouldReturnEmptyErrorLogs() {
        //Arrange
        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();
        CloudWatchLogsEvent.AWSLogs awsLogs = new CloudWatchLogsEvent.AWSLogs();
        awsLogs.withData("@H4sIAAAAAAAA/02Py2rCQBSGX2U46xTmnszshKZuKhQidCFS1Bx1qMmEM5O0xfrupYZCd/+Fb/FdocOUdidcfw0IHh4X68Xbqm6axbKGAuJHjwQejDacu8raUhgo4BJPS4rjAP43PuCEfU7z3mTCXQce0j0IKCCN+3SgMOQQ+6dwyUgJ/OY/ub2j9Vz85gqhBQ/KGS6lk7wyRnIlOXel5K4SXGkpK+F0xZ1V0hrpRKmctdJxpzUUkEOHKe+6AbwoS6G05sqZ0hZ/uuDhGSe8eFYTRWLfbDUfnr1SyPhCcQopxB7b9ZnieDoPY64/D4gttuwYic2inu0pHN4T3La3H4Eh/1xMAQAA");
        cloudWatchLogsEvent.setAwsLogs(awsLogs);

        //Act
        List<ErrorLog> errorLogs = errorLogService.parse(cloudWatchLogsEvent);

        //Assert
        assertThat(errorLogs, is(empty()));
    }

    @Test
    void parse_withCloudWatchLogsEventWithNoData_shouldReturnEmptyErrorLogs() {
        //Arrange
        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();
        CloudWatchLogsEvent.AWSLogs awsLogs = new CloudWatchLogsEvent.AWSLogs();
        cloudWatchLogsEvent.setAwsLogs(awsLogs);

        //Act
        List<ErrorLog> errorLogs = errorLogService.parse(cloudWatchLogsEvent);

        //Assert
        assertThat(errorLogs, is(empty()));
    }
}