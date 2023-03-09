package com.jayxu.kms.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;

class KmsTest {
    @Test
    void test() {
        try (var kms = KmsClient.builder().build();) {
            System.out.println(kms);

            var message = "nacos";

            var req = EncryptRequest.builder().keyId("alias/xujiajing")
                .plaintext(SdkBytes.fromUtf8String(message)).build();

            var resp = kms.encrypt(req);
            System.err.println(resp.encryptionAlgorithmAsString());
            var encoded = resp.ciphertextBlob();
            System.err.println(
                Base64.getEncoder().encodeToString(encoded.asByteArray()));

            var dreq = DecryptRequest.builder().ciphertextBlob(encoded).build();
            var dresp = kms.decrypt(dreq);

            var decoded = dresp.plaintext().asUtf8String();
            System.err.println(decoded);
            assertEquals(message, decoded);
        }
    }
}
