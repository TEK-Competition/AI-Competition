package com.laijiaxiang.supreme.controller;

import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.model.response.SupremeResponse;
import com.laijiaxiang.supreme.model.vo.ExternalUserInfoVO;
import com.laijiaxiang.supreme.service.ExternalUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户相关Api")
@RestController
@RequestMapping(value = {"/externalUser"})
public class ExternalUserController {

    private final ExternalUserService externalUserService;

    @Autowired
    public ExternalUserController(ExternalUserService externalUserService) {
        this.externalUserService = externalUserService;
    }

    @Operation(summary = "获取登录用户基本信息")
    @GetMapping(value = "/info")
    @ResponseBody
    public SupremeResponse<ExternalUserInfoVO> getUserInfo() {
        ExternalUserInfoVO result = externalUserService.getUserInfo(SupremeContextHandler.getUserId());
        return SupremeResponse.success(result);
    }

}
