package com.log.processor.business;

import com.log.processor.common.constants.AiAnalysisStatus;
import com.log.processor.common.entity.ErrorLog;
import com.log.processor.service.impl.ErrorLogServiceImpl;
import com.log.processor.service.impl.RagAiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AiProcessorTest {

    @Mock
    private RagAiServiceImpl ragAiService;

    @Mock
    private ErrorLogServiceImpl errorLogService;

    @InjectMocks
    private AiProcessor aiProcessor;

    @Captor
    private ArgumentCaptor<List<ErrorLog>> errorLogs;

    @Test
    void accept_WithErrorLogs_ShouldQueryKnowledgeBase_AndSaveErrorLogs() {
        //Arrange
        ErrorLog errorLog = new ErrorLog();
        String prompt = "Which is the hottest planet in Solar System?";
        errorLog.setMessage(prompt);
        Mockito.when(ragAiService.queryKnowledgeBase(Mockito.anyString())).thenReturn("Venus");

        //Act
        aiProcessor.accept(List.of(errorLog));

        //Assert
        Mockito.verify(ragAiService, Mockito.times(1)).queryKnowledgeBase(prompt);
        Mockito.verify(errorLogService, Mockito.times(1)).saveErrorLogs(errorLogs.capture());
        assertThat(errorLogs.getValue().get(0).getAiAnalysisResponse(), is("Venus"));
        assertThat(errorLogs.getValue().get(0).getAiAnalysisStatus(), is(AiAnalysisStatus.Completed));
    }
}