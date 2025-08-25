package com.laijiaxiang.supreme.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "注册表单")
@Getter
@Setter
@NoArgsConstructor
public class UserRegisterDTO {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名 username 不能为空")
    private String username;

    @Schema(description = "账号")
    @NotBlank(message = "账号 account 不能为空")
    private String account;

    @Schema(description = "密码")
    @NotBlank(message = "密码 password 不能为空")
    private String password;
}
