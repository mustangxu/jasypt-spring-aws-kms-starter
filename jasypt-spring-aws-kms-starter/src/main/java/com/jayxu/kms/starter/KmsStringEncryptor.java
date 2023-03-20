package com.jayxu.kms.starter;

import java.util.Base64;
import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import lombok.extern.slf4j.XSlf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;

/**
 * @author Jay Xu
 */
@XSlf4j
public final class KmsStringEncryptor implements StringEncryptor {
    public static final String KEY_DEFAULT_KEY_ID = "aws.kms.defaultKeyId";
    // message pattern: {ENC|DEC}([key alias]xxxxxxxxxxxxxxxxxxxxxxx)
    private static final Pattern KEY_ID_PATTERN = Pattern
        .compile("\\[(.+)\\](.+)");
    private KmsClient kms;
    @Value("${" + KEY_DEFAULT_KEY_ID + ":}")
    private String defaultKeyId;

    public KmsStringEncryptor(KmsClient kms) {
        this.kms = kms;
    }

    @Override
    public String encrypt(final String message) {
        var res = this.parseKeyId(message);
        return this.encrypt(res[0], res[1]);
    }

    public String encrypt(String keyId, final String message) {
        Assert.hasText(keyId, "keyId is required");

        var req = EncryptRequest.builder().keyId(keyId)
            .plaintext(SdkBytes.fromUtf8String(message)).build();

        var resp = this.kms.encrypt(req);

        return KmsStringEncryptor.log
            .exit((this.defaultKeyId.equals(keyId) ? "" : "[" + keyId + "]")
                + Base64.getEncoder()
                    .encodeToString(resp.ciphertextBlob().asByteArray()));
    }

    @Override
    public String decrypt(final String encryptedMessage) {
        var res = this.parseKeyId(encryptedMessage);
        return this.decrypt(res[0], res[1]);
    }

    public String decrypt(String keyId, final String encryptedMessage) {
        Assert.hasText(keyId, "keyId is required");

        KmsStringEncryptor.log.debug("Decrypting: [{}]{}", keyId,
            encryptedMessage);

        var req = DecryptRequest.builder().keyId(keyId)
            .ciphertextBlob(SdkBytes
                .fromByteArray(Base64.getDecoder().decode(encryptedMessage)))
            .build();

        var resp = this.kms.decrypt(req);

        return resp.plaintext().asUtf8String();
    }

    private String[] parseKeyId(final String message) {
        KmsStringEncryptor.log.entry("parseKeyId", message);

        String[] res;

        var matcher = KmsStringEncryptor.KEY_ID_PATTERN.matcher(message);
        if (matcher.matches()) {
            res = new String[] { matcher.group(1), matcher.group(2) };
        } else {
            res = new String[] { this.defaultKeyId, message };
        }

        return KmsStringEncryptor.log.exit(res);
    }

}
