package com.log.processor.common.utils;

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@Slf4j
public final class Utils {

    private Utils() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Optional<String> decompressCloudWatchLogsEvent(CloudWatchLogsEvent cloudWatchLogsEvent) {
        if (cloudWatchLogsEvent == null || cloudWatchLogsEvent.getAwsLogs() == null || cloudWatchLogsEvent.getAwsLogs().getData() == null) return Optional.empty();
        byte[] compressed;
        try {
            compressed = Base64.getDecoder().decode(cloudWatchLogsEvent.getAwsLogs().getData());
        } catch (IllegalArgumentException e) {
            log.error("Payload error, decode failed", e);
            return Optional.empty();
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
             GZIPInputStream gis = new GZIPInputStream(byteArrayInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return Optional.of(sb.toString());
        } catch (IOException e) {
            log.error("Failed to decompress cloudwatch log event:", e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> jsonStringToObject(String jsonString, Class<T> clazz) {
        try {
            log.info("Parsing json string: {}", jsonString);
            return Optional.ofNullable(OBJECT_MAPPER.readValue(jsonString, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse json string", e);
            return Optional.empty();
        }
    }
}
