package com.laijiaxiang.supreme.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WechatResponse {

    private String session_key;

    private String unionid;

    private String errmsg;

    private String openid;

    private int errcode;

}
