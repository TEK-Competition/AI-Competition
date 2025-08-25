package com.laijiaxiang.supreme.service.impl;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationOutput;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaiLianServiceImplTest {

    @InjectMocks
    private BaiLianServiceImpl baiLianService;

    private final String testApiKey = "test-api-key";
    private final String testAppId = "test-app-id";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(baiLianService, "apiKey", testApiKey);
        ReflectionTestUtils.setField(baiLianService, "appId", testAppId);
    }

    @Test
    void singleChat_Success() throws NoApiKeyException, InputRequiredException {
        // Given
        String prompt = "Hello, how are you?";
        String expectedResponse = "I'm fine, thank you!";

        ApplicationResult applicationResult = mock(ApplicationResult.class);
        ApplicationOutput applicationOutput = mock(ApplicationOutput.class);

        try (MockedConstruction<Application> mockedConstruction = mockConstruction(Application.class,
                (application, context) -> when(application.call(any(ApplicationParam.class))).thenReturn(applicationResult))) {

            when(applicationResult.getOutput()).thenReturn(applicationOutput);
            when(applicationOutput.getText()).thenReturn(expectedResponse);

            // When
            String result = baiLianService.singleChat(prompt);

            // Then
            assertEquals(expectedResponse, result);
            assertEquals(1, mockedConstruction.constructed().size());
            verify(mockedConstruction.constructed().get(0), times(1)).call(any(ApplicationParam.class));
        }
    }

    @Test
    void singleChat_ThrowsNoApiKeyException() throws NoApiKeyException, InputRequiredException {
        // Given
        String prompt = "Test prompt";

        try (MockedConstruction<Application> mockedConstruction = mockConstruction(Application.class,
                (application, context) -> when(application.call(any(ApplicationParam.class))).thenThrow(new NoApiKeyException()))) {

            // When & Then
            assertThrows(NoApiKeyException.class, () -> baiLianService.singleChat(prompt));
            assertEquals(1, mockedConstruction.constructed().size());
            verify(mockedConstruction.constructed().get(0), times(1)).call(any(ApplicationParam.class));
        }
    }

    @Test
    void singleChat_ThrowsInputRequiredException() throws NoApiKeyException, InputRequiredException {
        // Given
        String prompt = "Test prompt";

        try (MockedConstruction<Application> mockedConstruction = mockConstruction(Application.class,
                (application, context) -> when(application.call(any(ApplicationParam.class))).thenThrow(new InputRequiredException("Input is required")))) {

            // When & Then
            assertThrows(InputRequiredException.class, () -> baiLianService.singleChat(prompt));
            assertEquals(1, mockedConstruction.constructed().size());
            verify(mockedConstruction.constructed().get(0), times(1)).call(any(ApplicationParam.class));
        }
    }
}
