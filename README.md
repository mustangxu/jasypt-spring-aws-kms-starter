This project add support to jasypt-spring which utlize aws KMS for encrypt / decrypt

[![CodeQL](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/codeql.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/codeql.yml)
[![Maven Publish](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-publish.yml)
[![Maven Release](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-release.yml/badge.svg)](https://github.com/mustangxu/jasypt-spring-aws-kms-starter/actions/workflows/maven-release.yml)

## Usage
1. import lib in maven
```xml
<dependency>
    <groupId>com.jayxu</groupId>
    <artifactId>jasypt-spring-aws-kms-starter</artifactId>
    <version>{version}</version>
</dependency>
```
2. define `aws.kms.defaultKeyId` in bootstrap config file, else the default Jasypt StringEncryptor will be used
3. config aws `aws_access_key_id`, `aws_secret_access_key`, `region` properly according to [this article](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ec2-iam-roles.html)
4. you can use multiple keyIds in one project:
```
ENC(xxxxxxxxxx) // will use aws.kms.defaultKeyId to decrypt, same as ENC([${aws.kms.defaultKeyId}]xxxxxxxxxx)
ENC([another-key-id]xxxxxxxxxx) // will use another-key-id to decrypt
```

## Tools
1. start `nacos-demo` project as a spring boot project
2. open `htttp://localhost:8088/swagger-ui.html` in browser
3. the `default-controller` provides several tools for encrypt / decrypt by KMS (or default Jasypt StringEncryptor)
