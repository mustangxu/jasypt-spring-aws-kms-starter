package com.jayxu.kms.demo;

import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
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

import com.jayxu.kms.starter.EncryptorUtils;
import com.jayxu.kms.starter.KmsStringEncryptor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.SneakyThrows;

/**
 * @author Jay Xu
 */
@RefreshScope
@RestController
public class DefaultController {
    @Value("${" + KmsStringEncryptor.KEY_DEFAULT_KEY_ID + ":}")
    private String keyId;
    @Autowired
    private KmsStringEncryptor enc;
    @Autowired
    private Environment env;
    @Autowired
    private EncryptorUtils utils;

    /**
     * Get ${aws.kms.defaultKeyId}
     *
     * @return
     */
    @Operation(summary = "Get the value of ${ aws.kms.defaultKeyId }")
    @GetMapping("/keyId")
    public String getKeyId() {
        return this.keyId;
    }

    @Operation(summary = "Encrypt string")
    @PostMapping("/enc")
    public String encrypt(@Parameter(
            description = "KMS key id to use, use ${ aws.kms.defaultKeyId } if null") @RequestParam Optional<String> keyid,
            @RequestBody String raw) {
        return "[" + keyid.orElse(this.keyId) + "]"
            + this.enc.encrypt(keyid.orElse(this.keyId), raw);
    }

    @Operation(summary = "Encrypt string, in \"ENC(xxxxxxxxxx)\" format")
    @PostMapping("/dec")
    public String decrypt(@RequestBody String msg) {
        return this.enc.decrypt(msg);
    }

    @Operation(summary = "Decrypt values content in Properties format")
    @SneakyThrows
    @PostMapping("/dec/properties")
    public void decryptProperties(@RequestBody String msg, Writer writer) {
        var prop = new Properties();
        prop.load(new StringReader(msg));
        prop = this.utils.decryptObject(prop);

        prop.store(writer, null);
    }

    @Operation(summary = "Get value in Environment")
    @GetMapping("/env")
    public String getEnv(@RequestParam String key) {
        return this.env.getProperty(key);
    }

    @Operation(
            summary = "Encrypt the values in \"DEC(xxxxxxxxxx)\" format in a file")
    @PostMapping(path = "/enc/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public void encFile(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Support Yaml or Properties file") @RequestPart MultipartFile file,
            Writer writer) {
        try (var is = file.getInputStream()) {
            String filename = file.getOriginalFilename();

            switch (filename.substring(filename.lastIndexOf('.') + 1)) {
                case "yaml":
                case "yml":
                    var yaml = buildSnakeYaml();

                    Map<String, Object> map = yaml.load(is);
                    map = this.utils.encryptObject(map);

                    yaml.dump(map, writer);

                    break;
                case "properties":
                    var prop = new Properties();
                    prop.load(is);
                    prop = this.utils.encryptObject(prop);

                    prop.store(writer, null);

                    break;
                default:
                    writer.write(this.enc
                        .encrypt(IOUtils.toString(is, StandardCharsets.UTF_8)));

                    break;
            }

            writer.flush();
        }
    }

    private static Yaml buildSnakeYaml() {
        var dump = new DumperOptions();
        dump.setDefaultFlowStyle(FlowStyle.BLOCK);

        return new Yaml(dump);
    }
}
