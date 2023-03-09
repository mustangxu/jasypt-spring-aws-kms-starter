package com.jayxu.kms.starter;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
@ComponentScan(basePackages = "com.jayxu.kms.starter")
@Slf4j
@ConditionalOnClass(KmsStringEncryptor.class)
public class KmsConfig {
    @Primary
    @Bean("jasyptStringEncryptor") // override default jasyptStringEncryptor
    @ConditionalOnProperty({ KmsStringEncryptor.KEY_DEFAULT_KEY_ALIAS,
        KmsStringEncryptor.KEY_DEFAULT_KEY_ID }) // use default StringEncryptor if missed
    public StringEncryptor jasyptStringEncryptor() {
        log.info("Initializing KmsStringEncryptor");
        // 按https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ec2-iam-roles.html
        // 中定义的顺序尝试加载aws access相关配置，本地默认使用~/.aws/credentials配置文件
        return new KmsStringEncryptor(KmsClient.builder().build());
    }

}
