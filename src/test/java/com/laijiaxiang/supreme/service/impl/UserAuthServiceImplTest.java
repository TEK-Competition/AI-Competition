package com.laijiaxiang.supreme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laijiaxiang.supreme.constants.CommonConstants;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.model.dto.UserAccountLoginDTO;
import com.laijiaxiang.supreme.model.dto.UserRegisterDTO;
import com.laijiaxiang.supreme.model.dto.UserWechatLoginDTO;
import com.laijiaxiang.supreme.model.entity.ExternalUser;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;
import com.laijiaxiang.supreme.model.vo.UserTokenInfoVO;
import com.laijiaxiang.supreme.service.ExternalUserService;
import com.laijiaxiang.supreme.service.WechatService;
import com.laijiaxiang.supreme.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private WechatService wechatService;

    @Mock
    private ExternalUserService externalUserService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    private final int jwtTokenExpireTime = 30;

    private ExternalUser testUser;
    private String testPassword = "testPassword";
    private String encodedPassword = PasswordUtils.encodePassword(testPassword);

    @BeforeEach
    void setUp() {
        // 使用反射设置jwtTokenExpireTime字段
        try {
            java.lang.reflect.Field field = UserAuthServiceImpl.class.getDeclaredField("jwtTokenExpireTime");
            field.setAccessible(true);
            field.set(userAuthService, jwtTokenExpireTime);
        } catch (Exception e) {
            fail("Failed to set jwtTokenExpireTime field");
        }

        testUser = ExternalUser.builder()
                .id(1L)
                .username("testUser")
                .account("testAccount")
                .password(encodedPassword)
                .openid("testOpenid")
                .build();
    }

    @AfterEach
    void tearDown() {
        SupremeContextHandler.remove();
    }

    @Test
    void constructor_WithDependencies_ShouldInitializeService() {
        // Given
        UserAuthServiceImpl service = new UserAuthServiceImpl(redisTemplate, wechatService, externalUserService);

        // Then
        assertNotNull(service);
    }

    @Test
    void register_WithNewAccount_ShouldSaveUserAndReturnTrue() {
        // Given
        UserRegisterDTO registerDTO = createUserRegisterDTO();
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        boolean result = userAuthService.register(registerDTO);

        // Then
        assertTrue(result);
        ArgumentCaptor<ExternalUser> userCaptor = ArgumentCaptor.forClass(ExternalUser.class);
        verify(externalUserService, times(1)).save(userCaptor.capture());

        ExternalUser capturedUser = userCaptor.getValue();
        assertEquals(registerDTO.getUsername(), capturedUser.getUsername());
        assertEquals(registerDTO.getAccount(), capturedUser.getAccount());
        assertTrue(PasswordUtils.checkPassword(registerDTO.getPassword(), capturedUser.getPassword()));
    }

    @Test
    void register_WithExistingAccount_ShouldThrowBizException() {
        // Given
        UserRegisterDTO registerDTO = createUserRegisterDTO();
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAuthService.register(registerDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("该账号已经存在，请修改!", exception.getMessage());

        verify(externalUserService, never()).save(any(ExternalUser.class));
    }

    @Test
    void accountLogin_WithValidCredentials_ShouldReturnUserLoginVO() {
        // Given
        UserAccountLoginDTO loginDTO = createUserAccountLoginDTO();
        String miniProgramOpenid = "miniProgramOpenid";

        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(wechatService.getMiniProgramOpenid(loginDTO.getWechatCode())).thenReturn(miniProgramOpenid);
        when(externalUserService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>()); // No existing bindings
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        UserLoginVO result = userAuthService.accountLogin(loginDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(wechatService, times(1)).getMiniProgramOpenid(loginDTO.getWechatCode());
        verify(externalUserService, times(1)).list(any(LambdaQueryWrapper.class));
        // 修复：只调用一次updateById，用于绑定当前用户的微信
        verify(externalUserService, times(1)).updateById(any(ExternalUser.class));
        verify(redisTemplate, times(2)).opsForValue();

        // Verify token storage in Redis
        verify(valueOperations, times(1)).set(
                startsWith(CommonConstants.REDIS_CLIENT_ACCESS_TOKEN),
                any(UserTokenInfoVO.class),
                eq((long) jwtTokenExpireTime),
                eq(TimeUnit.MINUTES)
        );
        verify(valueOperations, times(1)).set(
                startsWith(CommonConstants.REDIS_CLIENT_REFRESH_TOKEN),
                any(UserTokenInfoVO.class),
                eq((long) jwtTokenExpireTime * 3L),
                eq(TimeUnit.MINUTES)
        );

        // Verify that the user's openid is updated
        assertEquals(miniProgramOpenid, testUser.getOpenid());
    }

    @Test
    void accountLogin_WithNonExistingUser_ShouldThrowBizException() {
        // Given
        UserAccountLoginDTO loginDTO = createUserAccountLoginDTO();
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAuthService.accountLogin(loginDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("用户不存在，请先注册！", exception.getMessage());

        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(externalUserService, never()).updateById(any(ExternalUser.class));
    }

    @Test
    void accountLogin_WithWrongPassword_ShouldThrowBizException() {
        // Given
        UserAccountLoginDTO loginDTO = createUserAccountLoginDTO();
        loginDTO.setPassword("wrongPassword");
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAuthService.accountLogin(loginDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("密码错误，请重新输入！", exception.getMessage());

        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(externalUserService, never()).updateById(any(ExternalUser.class));
    }

    @Test
    void accountLogin_WithExistingWechatBinding_ShouldUnbindPreviousAccountAndBindNewOne() {
        // Given
        UserAccountLoginDTO loginDTO = createUserAccountLoginDTO();
        String miniProgramOpenid = "miniProgramOpenid";
        ExternalUser previousUser = ExternalUser.builder()
                .id(2L)
                .username("previousUser")
                .account("previousAccount")
                .password(encodedPassword)
                .openid(miniProgramOpenid)
                .build();

        List<ExternalUser> previousUsers = new ArrayList<>();
        previousUsers.add(previousUser);

        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(wechatService.getMiniProgramOpenid(loginDTO.getWechatCode())).thenReturn(miniProgramOpenid);
        when(externalUserService.list(any(LambdaQueryWrapper.class))).thenReturn(previousUsers);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        UserLoginVO result = userAuthService.accountLogin(loginDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        // 修复：解绑操作是将openid设置为空字符串而不是null
        assertEquals("", previousUser.getOpenid()); // Previous user should be unbound (openid set to empty string)

        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(wechatService, times(1)).getMiniProgramOpenid(loginDTO.getWechatCode());
        verify(externalUserService, times(1)).list(any(LambdaQueryWrapper.class));
        verify(externalUserService, times(1)).updateById(previousUser); // Unbind previous user
        verify(externalUserService, times(2)).updateById(any(ExternalUser.class)); // Also bind current user + transactional
        verify(redisTemplate, times(2)).opsForValue();
    }

    @Test
    void accountLogin_WithNullExternalUserList_ShouldBindCurrentAccount() {
        // Given
        UserAccountLoginDTO loginDTO = createUserAccountLoginDTO();
        String miniProgramOpenid = "miniProgramOpenid";

        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(wechatService.getMiniProgramOpenid(loginDTO.getWechatCode())).thenReturn(miniProgramOpenid);
        when(externalUserService.list(any(LambdaQueryWrapper.class))).thenReturn(null); // Return null list
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        UserLoginVO result = userAuthService.accountLogin(loginDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(wechatService, times(1)).getMiniProgramOpenid(loginDTO.getWechatCode());
        verify(externalUserService, times(1)).list(any(LambdaQueryWrapper.class));
        // When list is null, we should not enter the unbinding loop, so only 1 update for binding current user
        verify(externalUserService, times(1)).updateById(any(ExternalUser.class));
        verify(redisTemplate, times(2)).opsForValue();
    }

    @Test
    void wechatLogin_WithValidBinding_ShouldReturnUserLoginVO() {
        // Given
        UserWechatLoginDTO loginDTO = createUserWechatLoginDTO();
        String miniProgramOpenid = "miniProgramOpenid";

        when(wechatService.getMiniProgramOpenid(loginDTO.getWechatCode())).thenReturn(miniProgramOpenid);
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        UserLoginVO result = userAuthService.wechatLogin(loginDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        verify(wechatService, times(1)).getMiniProgramOpenid(loginDTO.getWechatCode());
        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(redisTemplate, times(2)).opsForValue();
    }

    @Test
    void wechatLogin_WithNoBinding_ShouldThrowBizException() {
        // Given
        UserWechatLoginDTO loginDTO = createUserWechatLoginDTO();
        String miniProgramOpenid = "miniProgramOpenid";

        when(wechatService.getMiniProgramOpenid(loginDTO.getWechatCode())).thenReturn(miniProgramOpenid);
        when(externalUserService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> userAuthService.wechatLogin(loginDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("未绑定微信，请先绑定微信！", exception.getMessage());

        verify(wechatService, times(1)).getMiniProgramOpenid(loginDTO.getWechatCode());
        verify(externalUserService, times(1)).getOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void logout_WithValidToken_ShouldDeleteTokensFromRedisAndReturnTrue() {
        // Given
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();

        SupremeContextHandler.setToken(userLoginVO);

        String accessTokenKey = CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + userLoginVO.getAccessToken();
        String refreshTokenKey = CommonConstants.REDIS_CLIENT_REFRESH_TOKEN + userLoginVO.getRefreshToken();

        when(redisTemplate.delete(accessTokenKey)).thenReturn(true);
        when(redisTemplate.delete(refreshTokenKey)).thenReturn(true);

        // When
        boolean result = userAuthService.logout();

        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).delete(accessTokenKey);
        verify(redisTemplate, times(1)).delete(refreshTokenKey);
    }

    @Test
    void logout_WithInvalidAccessToken_ShouldReturnFalseAndNotDeleteRefreshToken() {
        // Given
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();

        SupremeContextHandler.setToken(userLoginVO);

        String accessTokenKey = CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + userLoginVO.getAccessToken();

        when(redisTemplate.delete(accessTokenKey)).thenReturn(false); // First delete returns false

        // When
        boolean result = userAuthService.logout();

        // Then
        assertFalse(result);
        verify(redisTemplate, times(1)).delete(accessTokenKey);
        // Due to short-circuit evaluation, refresh token should not be deleted when access token deletion fails
        verify(redisTemplate, never()).delete(contains("refresh_token"));
    }

    @Test
    void logout_WithInvalidRefreshToken_ShouldReturnFalse() {
        // Given
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();

        SupremeContextHandler.setToken(userLoginVO);

        String accessTokenKey = CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + userLoginVO.getAccessToken();
        String refreshTokenKey = CommonConstants.REDIS_CLIENT_REFRESH_TOKEN + userLoginVO.getRefreshToken();

        when(redisTemplate.delete(accessTokenKey)).thenReturn(true);
        when(redisTemplate.delete(refreshTokenKey)).thenReturn(false); // Second delete returns false

        // When
        boolean result = userAuthService.logout();

        // Then
        assertFalse(result);
        verify(redisTemplate, times(1)).delete(accessTokenKey);
        verify(redisTemplate, times(1)).delete(refreshTokenKey);
    }

    private UserRegisterDTO createUserRegisterDTO() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testUser");
        dto.setAccount("testAccount");
        dto.setPassword(testPassword);
        return dto;
    }

    private UserAccountLoginDTO createUserAccountLoginDTO() {
        UserAccountLoginDTO dto = new UserAccountLoginDTO();
        dto.setAccount("testAccount");
        dto.setPassword(testPassword);
        dto.setWechatCode("testWechatCode");
        return dto;
    }

    private UserWechatLoginDTO createUserWechatLoginDTO() {
        UserWechatLoginDTO dto = new UserWechatLoginDTO();
        dto.setWechatCode("testWechatCode");
        return dto;
    }
}
