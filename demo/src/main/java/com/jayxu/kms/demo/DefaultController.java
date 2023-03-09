package com.jayxu.kms.demo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.jayxu.kms.starter.EncryptorUtils;
import com.jayxu.kms.starter.KmsStringEncryptor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.XSlf4j;

@RefreshScope
@RestController
@XSlf4j
public class DefaultController {
    @Value("${" + KmsStringEncryptor.KEY_DEFAULT_KEY_ALIAS + ":#{null}}")
    private String alias;
    @Autowired
    private KmsStringEncryptor enc;
    @Autowired
    private Environment env;
    @Autowired
    private EncryptorUtils utils;
    @Autowired
    private NacosConfigManager nacosManager;
    @Value("${spring.cloud.nacos.config.group}")
    private String group;

    @GetMapping("/alias")
    public String getAlias() {
        return this.alias;
    }

    @GetMapping("/enc")
    public String encrypt(
            @RequestParam(defaultValue = "nacos-demo-key") String keyid,
            @RequestParam String raw) {
        return "ENC([" + keyid + "]" + this.enc.encrypt(keyid, raw) + ')';
    }

    @PostMapping("/dec")
    public String decrypt(
            @RequestParam(defaultValue = "nacos-demo-key") String keyid,
            @RequestBody String msg) {
        return this.enc.decrypt("alias/" + keyid, msg);
    }

    @GetMapping("/env")
    public String getEnv(@RequestParam String key) {
        return this.env.getProperty(key);
    }

    @GetMapping("/enc/file")
    @SneakyThrows
    public String encFile(@RequestParam String filename) {
        try (var is = DefaultController.class.getResourceAsStream(filename)) {
            return this.doEncFile(filename, is);
        }
    }

    @PostMapping("/enc/content")
    @SneakyThrows
    public String encContent(@RequestPart MultipartFile file) {
        try (var is = file.getInputStream()) {
            return this.doEncFile(file.getOriginalFilename(), is);
        }
    }

    @SneakyThrows
    private String doEncFile(String filename, InputStream is) {
        if (is == null) {
            throw new FileNotFoundException(filename);
        }

        switch (filename.substring(filename.lastIndexOf('.') + 1)) {
            case "yaml":
            case "yml":
                var dump = new DumperOptions();
                dump.setDefaultFlowStyle(FlowStyle.BLOCK);
                var yaml = new Yaml(dump);

                Map<String, Object> map = yaml.load(is);
                map = this.utils.encryptObject(map);

                return DefaultController.log.exit(yaml.dump(map));
            case "properties":
                var prop = new Properties();
                prop.load(is);
                prop = this.utils.encryptObject(prop);

                return DefaultController.log.exit(prop.toString());
            default:
                return log.exit(this.enc
                    .encrypt(IOUtils.toString(is, StandardCharsets.UTF_8)));
        }
    }

    @SneakyThrows
    @PostMapping("/apollo")
    public boolean postApollo(@RequestBody ApolloConfig apollo) {
        return this.nacosManager.getConfigService().publishConfig(
            apollo.getNamespaceName(), apollo.getAppId(),
            apollo.getItems().get(0).getValue());
    }
}
