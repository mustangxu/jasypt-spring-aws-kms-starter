package com.jayxu.kms.starter;

import java.util.Map;
import java.util.Properties;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.jayxu.kms.starter.EncryptorUtils;
import com.jayxu.kms.starter.KmsConfig;
import com.jayxu.kms.starter.KmsStringEncryptor;

import org.yaml.snakeyaml.Yaml;

import lombok.extern.slf4j.XSlf4j;

@SpringBootTest(classes = KmsConfig.class)
@XSlf4j
@SuppressWarnings("resource")
class KmsStringEncryptorTest {
    @Autowired
    private StringEncryptor enc;
    @Autowired
    private EncryptorUtils utils;

    @Test
    void test() {
        KmsStringEncryptorTest.log.entry(this.enc.getClass());
        var p = new Profiler("KmsTest");
        System.err.println(this.enc.getClass());

        final var raw = "hello world";
        var pj = p.startNested("jasypt");
        pj.start("encrypt");
        var encrypt = this.enc.encrypt(raw);
//        System.out.println(encrypt);

        pj.start("decrypt");
        Assertions.assertEquals(raw, this.enc.decrypt(encrypt), "result");

        if (this.enc instanceof KmsStringEncryptor) {
            var pk = p.startNested("kms");

            var keyid = "xujiajing";
            pk.start("encrypt");

            var kms = (KmsStringEncryptor) this.enc;
            encrypt = kms.encrypt(keyid, raw);
//            System.out.println(encrypt);
            try {
                kms.decrypt("", encrypt.substring(encrypt.indexOf(']') + 1));
                Assertions.fail("should fail");
            } catch (Exception ex) {
                KmsStringEncryptorTest.log.catching(ex);
                pk.start("decrypt");
                Assertions.assertEquals(raw, kms.decrypt(encrypt), "result");
            }
        }

        p.stop().print();
    }

    @Test
    void testEncYaml() {
        var dump = new DumperOptions();
        dump.setDefaultFlowStyle(FlowStyle.BLOCK);
        var yaml = new Yaml(dump);

        Map<String, Object> map = yaml.load(KmsStringEncryptorTest.class
            .getClassLoader().getResourceAsStream("bootstrap.yaml"));
        map = this.utils.encryptObject(map);

        KmsStringEncryptorTest.log.info(yaml.dump(map));
    }

    @Test
    void testEncProperties() throws Exception {
        var prop = new Properties();
        prop.load(KmsStringEncryptorTest.class.getClassLoader()
            .getResourceAsStream("test.properties"));

        prop = this.utils.encryptObject(prop);
        KmsStringEncryptorTest.log.info("{}", prop);
    }
}
