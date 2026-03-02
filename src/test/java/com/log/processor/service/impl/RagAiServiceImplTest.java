package com.log.processor.service.impl;

import com.log.processor.common.exception.AiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RagAiServiceImplTest {

    @Mock
    private BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

    @InjectMocks
    private RagAiServiceImpl ragAiService;

    @Test
    void queryKnowledgeBase_withPrompt_shouldReturnResponse() {
        //Arrange
        RetrieveAndGenerateOutput retrieveAndGenerateOutput = RetrieveAndGenerateOutput.builder()
                .text("8").build();
        RetrieveAndGenerateResponse retrieveAndGenerateResponse = RetrieveAndGenerateResponse
                .builder().output(retrieveAndGenerateOutput).build();
        Mockito.when(bedrockAgentRuntimeClient.retrieveAndGenerate(Mockito.any(RetrieveAndGenerateRequest.class)))
                .thenReturn(retrieveAndGenerateResponse);

        //Act
        String response = ragAiService.queryKnowledgeBase("How many planets in Solar System?");

        //Assert
        assertThat(response, is("8"));
    }

    @Test
    void queryKnowledgeBase_withPrompt_shouldThrowExceptionIfAnyErrors() {
        //Arrange
        Mockito.when(bedrockAgentRuntimeClient.retrieveAndGenerate(Mockito.any(RetrieveAndGenerateRequest.class)))
                .thenThrow(new RuntimeException("No permissions to query knowledge base"));

        //Act and Assert
        assertThrows(AiException.class, () -> ragAiService.queryKnowledgeBase("Which is the coldest planet in Solar System?"));
    }

}