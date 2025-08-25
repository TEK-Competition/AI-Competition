package com.laijiaxiang.supreme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laijiaxiang.supreme.mapper.ExternalUserMapper;
import com.laijiaxiang.supreme.model.entity.ExternalUser;
import com.laijiaxiang.supreme.model.vo.ExternalUserInfoVO;
import com.laijiaxiang.supreme.service.ExternalUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalUserServiceImpl extends ServiceImpl<ExternalUserMapper, ExternalUser> implements ExternalUserService {

    private final ExternalUserMapper externalUserMapper;

    @Autowired
    public ExternalUserServiceImpl(ExternalUserMapper externalUserMapper) {
        this.externalUserMapper = externalUserMapper;
    }

    @Override
    public ExternalUserInfoVO getUserInfo(Long userId) {
        ExternalUser externalUser = externalUserMapper.selectById(userId);
        return ExternalUserInfoVO.builder()
                .id(externalUser.getId())
                .avatar(externalUser.getAvatar())
                .username(externalUser.getUsername())
                .build();
    }

}
