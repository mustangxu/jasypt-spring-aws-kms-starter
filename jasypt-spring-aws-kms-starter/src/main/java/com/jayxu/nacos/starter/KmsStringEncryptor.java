package com.jayxu.nacos.starter;

import java.util.Base64;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.XSlf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;

@XSlf4j
public final class KmsStringEncryptor implements StringEncryptor {
    public static final String KEY_DEFAULT_KEY_ALIAS = "aws.kms.defaultKeyAlias";
    // message pattern: {ENC|DEC}([key alias]xxxxxxxxxxxxxxxxxxxxxxx)
    private static final Pattern KEY_ID_PATTERN = Pattern
        .compile("\\[(.+)\\](.+)");
    private static final String ALIAS_PREFIX = "alias/";
    private KmsClient kms;
    @Value("${" + KEY_DEFAULT_KEY_ALIAS + ":nacos-demo-key}")
    private String defaultKeyAlias;

    public KmsStringEncryptor(KmsClient kms) {
        this.kms = kms;
    }

    @Override
    public String encrypt(final String message) {
        var res = this.parseKeyId(message);
        return this.encrypt(res[0], res[1]);
    }

    public String encrypt(String keyId, final String message) {
        var newKeyId = KmsStringEncryptor.ALIAS_PREFIX + keyId;

        var req = EncryptRequest.builder().keyId(newKeyId)
            .plaintext(SdkBytes.fromUtf8String(message)).build();

        var resp = this.kms.encrypt(req);

        return KmsStringEncryptor.log
            .exit((this.defaultKeyAlias.equals(keyId) ? "" : "[" + keyId + "]")
                + Base64.getEncoder()
                    .encodeToString(resp.ciphertextBlob().asByteArray()));
    }

    @Override
    public String decrypt(final String encryptedMessage) {
        var res = this.parseKeyId(encryptedMessage);
        return this.decrypt(res[0], res[1]);
    }

    public String decrypt(String keyId, final String encryptedMessage) {
        var newKeyId = KmsStringEncryptor.ALIAS_PREFIX + keyId;

        KmsStringEncryptor.log.debug("Decrypting: [{}]{}", newKeyId,
            StringUtils.abbreviateMiddle(encryptedMessage, "...", 64));

        var req = DecryptRequest.builder().keyId(newKeyId)
            .ciphertextBlob(SdkBytes
                .fromByteArray(Base64.getDecoder().decode(encryptedMessage)))
            .build();

        var resp = this.kms.decrypt(req);

        return resp.plaintext().asUtf8String();
    }

    private String[] parseKeyId(final String message) {
        KmsStringEncryptor.log.entry(message);
        var matcher = KmsStringEncryptor.KEY_ID_PATTERN.matcher(message);
        var alias = matcher.matches() ? matcher.group(1) : this.defaultKeyAlias;

        String[] res = { alias,
            matcher.matches() ? matcher.group(2) : message };

        return KmsStringEncryptor.log.exit(res);
    }

}
