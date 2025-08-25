package com.laijiaxiang.supreme.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@TableName("c_external_user")
@Alias("externalUser")
public class ExternalUser {

    private Long id;

    private String username;
    private String account;
    private String password;
    private String avatar;
    private String openid;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

}
