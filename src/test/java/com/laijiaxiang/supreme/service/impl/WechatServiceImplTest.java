package com.laijiaxiang.supreme.service.impl;

import com.alibaba.fastjson2.JSON;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.model.response.WechatResponse;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WechatServiceImplTest {

    @Mock
    private OkHttpClient httpClient;

    @InjectMocks
    private WechatServiceImpl wechatService;

    private final String testAppId = "testAppId";
    private final String testAppSecret = "testAppSecret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wechatService, "appId", testAppId);
        ReflectionTestUtils.setField(wechatService, "appSecret", testAppSecret);
    }

    @Test
    void constructor_WithHttpClient_ShouldInitializeService() {
        // Given
        WechatServiceImpl service = new WechatServiceImpl(httpClient);

        // Then
        assertNotNull(service);
    }

    @Test
    void getMiniProgramOpenid_WithValidResponse_ShouldReturnOpenid() throws IOException {
        // Given
        String wechatCode = "testWechatCode";
        String expectedOpenid = "testOpenid";
        WechatResponse wechatResponse = new WechatResponse();
        wechatResponse.setErrcode(0);
        wechatResponse.setOpenid(expectedOpenid);
        wechatResponse.setSession_key("testSessionKey");

        String jsonResponse = JSON.toJSONString(wechatResponse);

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(jsonResponse, MediaType.get("application/json")))
                .build();

        when(httpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // When
        String result = wechatService.getMiniProgramOpenid(wechatCode);

        // Then
        assertEquals(expectedOpenid, result);
        verify(httpClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    void getMiniProgramOpenid_WithWechatError_ShouldThrowBizException() throws IOException {
        // Given
        String wechatCode = "testWechatCode";
        int errorCode = 40029;
        String errorMessage = "invalid code";
        WechatResponse wechatResponse = new WechatResponse();
        wechatResponse.setErrcode(errorCode);
        wechatResponse.setErrmsg(errorMessage);

        String jsonResponse = JSON.toJSONString(wechatResponse);

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(jsonResponse, MediaType.get("application/json")))
                .build();

        when(httpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> wechatService.getMiniProgramOpenid(wechatCode));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("获取小程序openid异常"));
        assertTrue(exception.getMessage().contains(String.valueOf(errorCode)));
        assertTrue(exception.getMessage().contains(errorMessage));

        verify(httpClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    void getMiniProgramOpenid_WithIOException_ShouldThrowBizException() throws IOException {
        // Given
        String wechatCode = "testWechatCode";

        Call mockCall = mock(Call.class);
        when(httpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> wechatService.getMiniProgramOpenid(wechatCode));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("获取小程序openid异常"));

        verify(httpClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }
}
