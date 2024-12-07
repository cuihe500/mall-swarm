package com.macro.mall.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限安全配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    // 移除拦截器配置，统一使用网关的token验证
} 