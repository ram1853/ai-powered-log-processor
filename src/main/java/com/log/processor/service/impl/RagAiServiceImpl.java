package com.log.processor.service.impl;

import com.log.processor.common.constants.Constants;
import com.log.processor.common.exception.AiException;
import com.log.processor.service.IRagAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

@Service
@Slf4j
public class RagAiServiceImpl implements IRagAiService {

    @Value("${knowledge_base_id}")
    private String knowledge_base_id;

    private final String TEMPLATE = """
            You are a production support assistant.
            
            Use ONLY the information present in the retrieved documents.

            If the retrieved documents contain a clearly relevant error that matches the user's issue semantically, return its solution.

            If the documents do not contain a sufficiently relevant match, respond exactly:
            'Human Intervention Needed'

            Do NOT invent solutions.
            Do NOT use outside knowledge.
            Limit the answer to 1 line.
            
            $search_results$
            """;

    private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

    @Autowired
    public RagAiServiceImpl(BedrockAgentRuntimeClient bedrockAgentRuntimeClient) {
        this.bedrockAgentRuntimeClient = bedrockAgentRuntimeClient;
    }

    @Override
    public String queryKnowledgeBase(String message) {
        try {
            RetrieveAndGenerateInput retrieveAndGenerateInput =
                    RetrieveAndGenerateInput.builder()
                            .text(message)
                            .build();

            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .textPromptTemplate(TEMPLATE).build();

            TextInferenceConfig textInferenceConfig = TextInferenceConfig
                    .builder()
                    .temperature(0.0f)
                    .maxTokens(200)
                    .build();

            InferenceConfig inferenceConfig = InferenceConfig.builder()
                    .textInferenceConfig(textInferenceConfig)
                    .build();

            GenerationConfiguration generationConfiguration = GenerationConfiguration
                    .builder()
                    .promptTemplate(promptTemplate)
                    .inferenceConfig(inferenceConfig)
                    .build();

            KnowledgeBaseRetrieveAndGenerateConfiguration knowledgeBaseRetrieveAndGenerateConfiguration =
                    KnowledgeBaseRetrieveAndGenerateConfiguration.builder()
                            .knowledgeBaseId(knowledge_base_id)
                            .generationConfiguration(generationConfiguration)
                            .modelArn(Constants.MODEL_ARN)
                            .build();

            RetrieveAndGenerateConfiguration retrieveAndGenerateConfiguration =
                    RetrieveAndGenerateConfiguration.builder()
                            .knowledgeBaseConfiguration(knowledgeBaseRetrieveAndGenerateConfiguration)
                            .type(RetrieveAndGenerateType.KNOWLEDGE_BASE)
                            .build();

            RetrieveAndGenerateRequest retrieveAndGenerateRequest = RetrieveAndGenerateRequest
                    .builder()
                    .retrieveAndGenerateConfiguration(retrieveAndGenerateConfiguration)
                    .input(retrieveAndGenerateInput)
                    .build();

            RetrieveAndGenerateResponse retrieveAndGenerateResponse = bedrockAgentRuntimeClient
                    .retrieveAndGenerate(retrieveAndGenerateRequest);

            return retrieveAndGenerateResponse.output().text();
        } catch (Exception e) {
            log.error("Error while querying knowledge base", e);
            throw new AiException("Error while querying knowledge base with message: "+message);
        }
    }
}
