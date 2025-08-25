package com.laijiaxiang.supreme.controller;

import com.laijiaxiang.supreme.model.dto.UserAccountLoginDTO;
import com.laijiaxiang.supreme.model.dto.UserRegisterDTO;
import com.laijiaxiang.supreme.model.dto.UserWechatLoginDTO;
import com.laijiaxiang.supreme.model.response.SupremeResponse;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;
import com.laijiaxiang.supreme.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "登录注册相关Api")
@RestController
@RequestMapping(value = {"/auth"})
public class UserAuthController {

    private final UserAuthService authService;

    @Autowired
    public UserAuthController(UserAuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public SupremeResponse<Boolean> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        boolean result = authService.register(registerDTO);
        return SupremeResponse.success(result);
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/account/login")
    public SupremeResponse<UserLoginVO> accountLogin(@Valid @RequestBody UserAccountLoginDTO loginDTO) {
        UserLoginVO result = authService.accountLogin(loginDTO);
        return SupremeResponse.success(result);
    }

    @Operation(summary = "微信快速登录")
    @PostMapping("/wechat/login")
    public SupremeResponse<UserLoginVO> wechatLogin(@Valid @RequestBody UserWechatLoginDTO loginDTO) {
        UserLoginVO result = authService.wechatLogin(loginDTO);
        return SupremeResponse.success(result);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public SupremeResponse<Boolean> logout() {
        boolean result = authService.logout();
        return SupremeResponse.success(result);
    }

}
