package com.laijiaxiang.supreme.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "微信登录表单")
@Getter
@Setter
@NoArgsConstructor
public class UserWechatLoginDTO {

    @Schema(description = "微信code")
    @NotBlank(message = "微信code wechatCode 不能为空")
    private String wechatCode;

}
