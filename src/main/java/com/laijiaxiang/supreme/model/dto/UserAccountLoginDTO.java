package com.laijiaxiang.supreme.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "登录表单")
@Getter
@Setter
@NoArgsConstructor
public class UserAccountLoginDTO {

    @Schema(description = "账号")
    @NotBlank(message = "账号 account 不能为空")
    private String account;

    @Schema(description = "密码")
    @NotBlank(message = "密码 password 不能为空")
    private String password;

    @Schema(description = "微信code")
    @NotBlank(message = "微信code wechatCode 不能为空")
    private String wechatCode;
}
