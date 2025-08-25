package com.laijiaxiang.supreme.interceptor;

import com.laijiaxiang.supreme.constants.CommonConstants;
import com.laijiaxiang.supreme.exception.TokenValidateException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;
import com.laijiaxiang.supreme.model.vo.UserTokenInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenInterceptorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private TokenInterceptor tokenInterceptor;

    private MockedStatic<SupremeContextHandler> mockedSupremeContextHandler;

    @BeforeEach
    void setUp() {
        tokenInterceptor = new TokenInterceptor(redisTemplate);
        mockedSupremeContextHandler = mockStatic(SupremeContextHandler.class);
    }

    @AfterEach
    void tearDown() {
        mockedSupremeContextHandler.close();
    }

    @Test
    void constructor_WithRedisTemplate_ShouldInitializeInterceptor() {
        // When
        TokenInterceptor interceptor = new TokenInterceptor(redisTemplate);

        // Then
        assertNotNull(interceptor);
    }

    @Test
    void preHandle_WithNoAuthorizationHeader_ShouldThrowTokenValidateException() {
        // Given
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn(null);

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token不存在，请先登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithEmptyAuthorizationHeader_ShouldThrowTokenValidateException() {
        // Given
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("");

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token不存在，请先登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithInvalidAuthorizationFormat_ShouldThrowTokenValidateException() {
        // Given
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("InvalidFormat");

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token格式不合法，请重新登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithMalformedToken_ShouldThrowTokenValidateException() {
        // Given
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("Bearer ");

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token无效，请重新登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithEmptyToken_ShouldThrowTokenValidateException() {
        // Given
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("Bearer  ");

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token无效，请重新登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithValidTokenButNoRedisData_ShouldThrowTokenValidateException() throws Exception {
        // Given
        String token = "validToken";
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("Bearer " + token);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + token)).thenReturn(null);

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token已失效，请重新登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void preHandle_WithValidTokenInRedis_ShouldSetContextAndReturnTrue() throws Exception {
        // Given
        String token = "validToken";
        Long userId = 1L;
        String refreshToken = "refreshToken";
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("Bearer " + token);

        UserTokenInfoVO tokenInfo = new UserTokenInfoVO();
        tokenInfo.setId(userId);
        tokenInfo.setAccessToken(token);
        tokenInfo.setRefreshToken(refreshToken);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + token)).thenReturn(tokenInfo);

        // When
        boolean result = tokenInterceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(result);
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(userId), times(1));
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setToken(any(UserLoginVO.class)), times(1));
    }

    @Test
    void preHandle_WithRedisException_ShouldThrowTokenValidateException() throws Exception {
        // Given
        String token = "validToken";
        when(request.getHeader(CommonConstants.AUTHORIZATION)).thenReturn("Bearer " + token);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + token)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        TokenValidateException exception = assertThrows(TokenValidateException.class, () -> {
            tokenInterceptor.preHandle(request, response, new Object());
        });

        assertEquals("token校验失败，请重新登录！", exception.getMessage());
        mockedSupremeContextHandler.verify(() -> SupremeContextHandler.setUserId(anyLong()), never());
    }

    @Test
    void afterCompletion_ShouldRemoveContext() throws Exception {
        // When
        tokenInterceptor.afterCompletion(request, response, new Object(), null);

        // Then
        mockedSupremeContextHandler.verify(SupremeContextHandler::remove, times(1));
    }
}
