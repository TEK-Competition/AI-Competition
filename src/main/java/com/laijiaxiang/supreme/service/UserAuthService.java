package com.laijiaxiang.supreme.service;


import com.laijiaxiang.supreme.model.dto.UserAccountLoginDTO;
import com.laijiaxiang.supreme.model.dto.UserRegisterDTO;
import com.laijiaxiang.supreme.model.dto.UserWechatLoginDTO;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;

public interface UserAuthService {

    boolean register(UserRegisterDTO registerDTO);

    UserLoginVO accountLogin(UserAccountLoginDTO loginDTO);

    UserLoginVO wechatLogin(UserWechatLoginDTO loginDTO);

    boolean logout();

}
