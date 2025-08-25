package com.laijiaxiang.supreme.model.vo;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenInfoVO {

    private Long id;

    private String accessToken;

    private String refreshToken;

}
