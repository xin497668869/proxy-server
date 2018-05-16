package com.xin.test.proxy.cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.xin.test.proxy.https.Main.hexStringToBytes;
import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
//        SecretKey secretKey = keyGenerator.generateKey();
//        System.out.println(secretKey);
        SecretKey secretKey = new SecretKeySpec(hexStringToBytes("cb96357f632d5a52e7afe0c260a4dda8"), "AES");
        //NoPadding
        Cipher c = Cipher.getInstance("AES/GCM/PKCS5Padding");
        SecureRandom secureRandom = new SecureRandom();
        c.init(ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, hexStringToBytes("adsf")), secureRandom);

        byte[] bytes = c.doFinal("a".getBytes());
//        c.init(ENCRYPT_MODE, secretKey , new GCMParameterSpec(128, hexStringToBytes("cb96357f632afe0c8")),new SecureRandom());
//        c.init(ENCRYPT_MODE, secretKey , new GCMParameterSpec(128, hexStringToBytes("cb96357f632d5a52e7afe0c260a4dda8")));
//        byte[] byte22s = c.doFinal("cccaccaaaaaaa909090addsa".getBytes());
//        byte[] byte222s = c.doFinal("cccaccaaaaaaa909090addda".getBytes());
        System.out.println(bytes);

    }

}
