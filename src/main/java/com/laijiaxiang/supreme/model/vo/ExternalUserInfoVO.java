package com.laijiaxiang.supreme.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "用户信息")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalUserInfoVO {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String username;

}
