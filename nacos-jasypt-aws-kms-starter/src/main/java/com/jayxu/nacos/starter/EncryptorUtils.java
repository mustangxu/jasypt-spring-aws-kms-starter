package com.jayxu.nacos.starter;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.XSlf4j;

@Component
@XSlf4j
public class EncryptorUtils {
    private static final Pattern ENC_PATTERN = Pattern.compile("DEC\\((.+)\\)");
    @Autowired
    private StringEncryptor enc;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T encryptObject(T o) {
        if (o == null) {
            return o;
        }

        EncryptorUtils.log.entry(o, o.getClass());

        if (o instanceof String) {
            return (T) this.encryptString((String) o);
        }

        if (o instanceof Properties) {
            return (T) this.encryptProperties((Properties) o);
        }

        if (o instanceof Map) {
            return (T) this.encryptMap((Map) o);
        }

        if (o instanceof List) {
            var l = (List) o;
            l.replaceAll(this::encryptObject);

            return (T) l;
        }

        return o;
    }

    public Map<String, Object> encryptMap(Map<String, Object> map) {
        map.replaceAll((k, v) -> this.encryptObject(v));
        return map;
    }

    public String encryptString(String s) {
        var m = EncryptorUtils.ENC_PATTERN.matcher(s);
        if (m.matches()) {
            return "ENC("
                + EncryptorUtils.log.exit(this.enc.encrypt(m.group(1))) + ")";
        }

        return s;
    }

    public Properties encryptProperties(Properties prop) {
        prop.replaceAll((k, v) -> this.encryptString((String) v));
        return prop;
    }
}
