/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户表
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sys_user")
public class SysUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    // 用户名称
    @Column(name = "name", length = 64)
    private String name;
    // 账号
    @Column(name = "account", nullable = false, length = 32)
    private String account;
    // 密码
    @Column(name = "password", nullable = false, length = 64)
    private String password;
    // 登录时间
    @Column(name = "login_time")
    private Date loginTime;
    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;
}

