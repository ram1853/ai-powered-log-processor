package com.log.processor.common.utils;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.log.processor.common.dto.CloudWatchLogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UtilsTest {

    static List<CloudWatchLogsEvent> inputs() {
        CloudWatchLogsEvent cloudWatchLogsEvent1 = new CloudWatchLogsEvent();
        cloudWatchLogsEvent1.setAwsLogs(null);

        CloudWatchLogsEvent cloudWatchLogsEvent2 = new CloudWatchLogsEvent();
        CloudWatchLogsEvent.AWSLogs awsLogs = new CloudWatchLogsEvent.AWSLogs();
        awsLogs.setData("test");
        cloudWatchLogsEvent2.setAwsLogs(awsLogs);

        CloudWatchLogsEvent cloudWatchLogsEvent3 = new CloudWatchLogsEvent();
        CloudWatchLogsEvent.AWSLogs awsLogs3 = new CloudWatchLogsEvent.AWSLogs();
        awsLogs.setData(null);
        cloudWatchLogsEvent3.setAwsLogs(awsLogs3);

        return Arrays.asList(null, cloudWatchLogsEvent1, cloudWatchLogsEvent2, cloudWatchLogsEvent3);
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void decompressCloudWatchLogsEvent_withNoCloudWatchLogsEvent_shouldReturnEmptyOptional(CloudWatchLogsEvent cloudWatchLogsEvent) {
        //Act
        Optional<String> decompressed = Utils.decompressCloudWatchLogsEvent(cloudWatchLogsEvent);

        //Assert
        assertThat(decompressed, is(Optional.empty()));
    }

    @Test
    void decompressCloudWatchLogsEvent_withInvalidInputStream_shouldReturnEmptyOptional() {
        //Arrange
        String plainText = "this is not gzip";
        String base64 = Base64.getEncoder()
                .encodeToString(plainText.getBytes(StandardCharsets.UTF_8));

        CloudWatchLogsEvent.AWSLogs awsLogs = new CloudWatchLogsEvent.AWSLogs();
        awsLogs.setData(base64);

        CloudWatchLogsEvent cloudWatchLogsEvent = new CloudWatchLogsEvent();
        cloudWatchLogsEvent.setAwsLogs(awsLogs);

        //Act
        Optional<String> decompressed = Utils.decompressCloudWatchLogsEvent(cloudWatchLogsEvent);

        //Assert
        assertThat(decompressed, is(Optional.empty()));
    }

    @Test
    void jsonStringToObject_withInvalidJsonString_shouldReturnEmptyOptional() {
        //Arrange
        String jsonString = "not a valid json";

        //Act
        Optional<CloudWatchLogPayload> result = Utils.jsonStringToObject(jsonString, CloudWatchLogPayload.class);

        //Assert
        assertThat(result, is(Optional.empty()));
    }
}