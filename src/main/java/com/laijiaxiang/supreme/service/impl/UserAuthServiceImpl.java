package com.laijiaxiang.supreme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laijiaxiang.supreme.constants.CommonConstants;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.model.dto.UserAccountLoginDTO;
import com.laijiaxiang.supreme.model.dto.UserRegisterDTO;
import com.laijiaxiang.supreme.model.dto.UserWechatLoginDTO;
import com.laijiaxiang.supreme.model.entity.ExternalUser;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;
import com.laijiaxiang.supreme.model.vo.UserTokenInfoVO;
import com.laijiaxiang.supreme.service.ExternalUserService;
import com.laijiaxiang.supreme.service.UserAuthService;
import com.laijiaxiang.supreme.service.WechatService;
import com.laijiaxiang.supreme.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WechatService wechatService;
    private final ExternalUserService externalUserService;

    @Value("${expire-time.jwt-token}")
    private int jwtTokenExpireTime;

    @Autowired
    public UserAuthServiceImpl(RedisTemplate<String, Object> redisTemplate,
                               WechatService wechatService,
                               ExternalUserService externalUserService) {
        this.redisTemplate = redisTemplate;
        this.wechatService = wechatService;
        this.externalUserService = externalUserService;
    }

    @Override
    public boolean register(UserRegisterDTO registerDTO) {
        String account = registerDTO.getAccount();
        //判断账号是否已经存在？
        ExternalUser externalUser = externalUserService.getOne(
                new LambdaQueryWrapper<ExternalUser>()
                        .eq(ExternalUser::getAccount, account)
                        .eq(ExternalUser::getDeleted, false)
        );
        if (externalUser != null) {
            throw new BizException(HttpStatus.BAD_REQUEST, "该账号已经存在，请修改!");
        }
        externalUser = ExternalUser.builder()
                .username(registerDTO.getUsername())
                .account(account)
                .password(PasswordUtils.encodePassword(registerDTO.getPassword()))
                .build();
        externalUserService.save(externalUser);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO accountLogin(UserAccountLoginDTO loginDTO) {
        //校验参数
        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();
        log.info("登录账户号：{}", account);
        //获取用户信息
        ExternalUser externalUser = externalUserService.getOne(
                new LambdaQueryWrapper<ExternalUser>()
                        .eq(ExternalUser::getAccount, account)
                        .eq(ExternalUser::getDeleted, false)
        );
        if(externalUser == null){
            throw new BizException(HttpStatus.BAD_REQUEST, "用户不存在，请先注册！");
        }
        // 验证密码
        if (!PasswordUtils.checkPassword(password, externalUser.getPassword())) {
            throw new BizException(HttpStatus.BAD_REQUEST, "密码错误，请重新输入！");
        }
        Long userId = externalUser.getId();
        //获取小程序openid
        String wechatCode = loginDTO.getWechatCode();
        String miniProgramOpenid = wechatService.getMiniProgramOpenid(wechatCode);
        //先删除当前微信openid已经绑定的账号
        List<ExternalUser> externalUserList = externalUserService.list(
                new LambdaQueryWrapper<ExternalUser>()
                        .eq(ExternalUser::getOpenid, miniProgramOpenid)
                        .eq(ExternalUser::getDeleted, false)
        );
        if(externalUserList != null && !externalUserList.isEmpty()){
            for (ExternalUser exUser : externalUserList) {
                exUser.setOpenid("");
                externalUserService.updateById(exUser);
            }
        }
        //绑定微信
        externalUser.setOpenid(miniProgramOpenid);
        externalUserService.updateById(externalUser);
        return genToken(userId);
    }

    @Override
    public UserLoginVO wechatLogin(UserWechatLoginDTO loginDTO) {
        //获取小程序openid
        String wechatCode = loginDTO.getWechatCode();
        String miniProgramOpenid = wechatService.getMiniProgramOpenid(wechatCode);
        //获取绑定信息
        ExternalUser externalUser = externalUserService.getOne(
                new LambdaQueryWrapper<ExternalUser>()
                        .eq(ExternalUser::getOpenid, miniProgramOpenid)
                        .eq(ExternalUser::getDeleted, false)
        );
        if (externalUser == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "未绑定微信，请先绑定微信！");
        }
        Long userId = externalUser.getId();
        return genToken(userId);
    }

    @Override
    public boolean logout() {
        //redis中删除token
        UserLoginVO tokenInfo = SupremeContextHandler.getToken();
        String accessToken = tokenInfo.getAccessToken();
        String accessTokenKey = CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + accessToken;
        String refreshTokenKey = CommonConstants.REDIS_CLIENT_REFRESH_TOKEN + tokenInfo.getRefreshToken();
        return redisTemplate.delete(accessTokenKey)
                && redisTemplate.delete(refreshTokenKey);
    }

    private UserLoginVO genToken(Long userId) {
        //生成token
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        UserTokenInfoVO clientTokenInfo = UserTokenInfoVO.builder()
                .id(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        //token存入redis
        String accessTokenKey = CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + accessToken;
        redisTemplate.opsForValue().set(accessTokenKey, clientTokenInfo, jwtTokenExpireTime, TimeUnit.MINUTES);
        String refreshTokenKey = CommonConstants.REDIS_CLIENT_REFRESH_TOKEN + refreshToken;
        redisTemplate.opsForValue().set(refreshTokenKey, clientTokenInfo, jwtTokenExpireTime * 3L, TimeUnit.MINUTES);
        return UserLoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
