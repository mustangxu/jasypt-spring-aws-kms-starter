This project add support to jasypt-spring which utlize aws KMS for encrypt / decrypt

[![CodeQL](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/codeql.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/codeql.yml)
[![Maven Publish](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-publish.yml)
[![Maven Release](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-release.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-release.yml)

## Usage
1. import lib in maven
```xml
<dependency>
    <groupId>com.jayxu.nacos</groupId>
    <artifactId>nacos-jasypt-aws-kms-starter</artifactId>
    <version>{version}</version>
</dependency>
```
2. define `aws.kms.defaultKeyAlias` in bootstrap config file, else the default Jasypt StringEncryptor will be used
3. config aws `aws_access_key_id`, `aws_secret_access_key`, `region` properly according to [this article](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ec2-iam-roles.html)

## Tools
1. start `nacos-demo` project as a spring boot project
2. open `htttp://localhost:8088/swagger-ui.html` in browser
3. the `default-controller` provides several tools for encrypt / decrypt by KMS (or default Jasypt StringEncryptor)
