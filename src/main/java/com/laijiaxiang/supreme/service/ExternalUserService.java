package com.laijiaxiang.supreme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laijiaxiang.supreme.model.entity.ExternalUser;
import com.laijiaxiang.supreme.model.vo.ExternalUserInfoVO;

public interface ExternalUserService extends IService<ExternalUser> {

    ExternalUserInfoVO getUserInfo(Long userId);

}
