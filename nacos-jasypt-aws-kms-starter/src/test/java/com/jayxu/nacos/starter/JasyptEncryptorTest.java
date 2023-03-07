package com.jayxu.nacos.starter;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.salt.RandomSaltGenerator;
import org.junit.jupiter.api.Test;

public class JasyptEncryptorTest {
    @Test
    void test() {
        var enc = new StandardPBEStringEncryptor();
        enc.setSaltGenerator(new RandomSaltGenerator());
        enc.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        enc.setIvGenerator(new RandomIvGenerator());
        enc.setPassword("helloworld");
        System.out.println(enc.encrypt("nacos"));

        System.out.println(enc.decrypt(
            "wacUEQ0x+sgDSEM+swIPoQixmLoS+nzSDG4xdvtPv0lNDbfSgzWd5Hk3N2cgS8BC"));
    }
}
