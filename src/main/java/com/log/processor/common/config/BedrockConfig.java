package com.log.processor.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;

@Configuration
public class BedrockConfig {

    @Bean
    public BedrockAgentRuntimeClient bedrockClient() {
        return BedrockAgentRuntimeClient.builder().build();
    }
}
