/**
 * Copyright(c) 2010-2023 by Youxin Financial Group
 * All Rights Reserved
 */
package com.jayxu.nacos.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author jay
 */
@Configuration
public class MvcConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http.headers().xssProtection();
        return http.build();
    }
}
