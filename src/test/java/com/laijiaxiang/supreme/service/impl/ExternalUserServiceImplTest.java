package com.laijiaxiang.supreme.service.impl;

import com.laijiaxiang.supreme.mapper.ExternalUserMapper;
import com.laijiaxiang.supreme.model.entity.ExternalUser;
import com.laijiaxiang.supreme.model.vo.ExternalUserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalUserServiceImplTest {

    @Mock
    private ExternalUserMapper externalUserMapper;

    @InjectMocks
    private ExternalUserServiceImpl externalUserService;

    private ExternalUser testUser;

    @BeforeEach
    void setUp() {
        testUser = ExternalUser.builder()
                .id(1L)
                .username("testUser")
                .account("testAccount")
                .password("testPassword")
                .avatar("testAvatar")
                .openid("testOpenId")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    @Test
    void constructor_WithMapper_ShouldInitializeService() {
        // Given
        ExternalUserServiceImpl service = new ExternalUserServiceImpl(externalUserMapper);

        // Then
        assertNotNull(service);
    }

    @Test
    void getUserInfo_WithValidUserId_ShouldReturnUserInfoVO() {
        // Given
        Long userId = 1L;
        when(externalUserMapper.selectById(userId)).thenReturn(testUser);

        // When
        ExternalUserInfoVO result = externalUserService.getUserInfo(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getAvatar(), result.getAvatar());
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(externalUserMapper, times(1)).selectById(userId);
    }

    @Test
    void getUserInfo_WithNullUser_ShouldThrowNullPointerException() {
        // Given
        Long userId = 1L;
        when(externalUserMapper.selectById(userId)).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, () -> externalUserService.getUserInfo(userId));

        verify(externalUserMapper, times(1)).selectById(userId);
    }
}
