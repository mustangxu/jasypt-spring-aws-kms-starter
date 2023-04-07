package com.jayxu.kms.starter;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.XSlf4j;

/**
 * @author Jay Xu
 */
@Component
@XSlf4j
public class EncryptorUtils {
    private static final Pattern ENC_PATTERN = Pattern.compile("DEC\\((.+)\\)");
    private static final Pattern DEC_PATTERN = Pattern.compile("ENC\\((.+)\\)");
    @Autowired
    private StringEncryptor enc;

    public <T> T encryptObject(T o) {
        return this.processObject(o, this.enc::encrypt);
    }

    public <T> T decryptObject(T o) {
        return this.processObject(o, this.enc::decrypt);
    }

    @SuppressWarnings("rawtypes")
    public <T> T processObject(T o, UnaryOperator<String> encryptOrDecrypt) {
        if (o == null) {
            return null;
        }

        EncryptorUtils.log.entry("processObject", o, o.getClass());

        if (o instanceof String) {
            return (T) this.processString((String) o, encryptOrDecrypt);
        }

        if (o instanceof Properties) {
            return (T) this.processProperties((Properties) o, encryptOrDecrypt);
        }

        if (o instanceof Map) {
            return (T) this.processMap((Map) o, encryptOrDecrypt);
        }

        if (o instanceof List l) {
            l.replaceAll(this::encryptObject);

            return (T) l;
        }

        return o;
    }

    public Map<String, Object> processMap(Map<String, Object> map,
            UnaryOperator<String> encryptOrDecrypt) {
        map.replaceAll((k, v) -> this.processObject(v, encryptOrDecrypt));
        return map;
    }

    public String processString(String s,
            UnaryOperator<String> encryptOrDecrypt) {
        var m = EncryptorUtils.ENC_PATTERN.matcher(s);
        if (m.matches()) {
            return "ENC("
                + EncryptorUtils.log.exit(encryptOrDecrypt.apply(m.group(1)))
                + ")";
        }

        m = EncryptorUtils.DEC_PATTERN.matcher(s);
        if (m.matches()) {
            return encryptOrDecrypt.apply(m.group(1));
        }

        return s;
    }

    public Properties processProperties(Properties prop,
            UnaryOperator<String> encryptOrDecrypt) {
        prop.replaceAll(
            (k, v) -> this.processString((String) v, encryptOrDecrypt));
        return prop;
    }
}
