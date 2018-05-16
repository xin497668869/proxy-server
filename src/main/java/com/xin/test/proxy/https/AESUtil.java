package com.xin.test.proxy.https;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version V1.0
 * @desc AES 加密工具类
 */
public class AESUtil {

    private static final String KEY_ALGORITHM            = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//默认的加密算法

    /**
     * AES 加密操作
     *
     * @param content  待加密内容
     * @param password 加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static byte[] encrypt(byte[] content, byte[] password) {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password), new GCMParameterSpec(128, Main.hexStringToBytes("ff69ef1d0000000000000000")), new SecureRandom());

            byte[] result = cipher.doFinal(content);// 加密

            return result;//通过Base64转码返回
        } catch (Exception ex) {
            Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param password
     * @return
     */
    public static String decrypt(byte[] content, byte[] password) {

        try {
            //实例化
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password), new GCMParameterSpec(128, Main.hexStringToBytes("4401c27b0000000000000001")), new SecureRandom());
            cipher.updateAAD(Main.hexStringToBytes("0000000000000001170303004a"));
            //执行操作
            byte[] result = cipher.doFinal(content);

            return new String(result, "utf-8");
        } catch (Exception ex) {
            Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * 生成加密秘钥
     *
     * @return
     */
    public static SecretKeySpec getSecretKey(byte[] password) {
        if (true) {
            return new SecretKeySpec(password, KEY_ALGORITHM);
        }
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = null;

        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);

            //AES 要求密钥长度为 128
            kg.init(128, new SecureRandom(password));

            //生成一个密钥
            SecretKey secretKey = kg.generateKey();

            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// 转换为AES专用密钥
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void main(String[] args) {
        String s = "hello,您好";

        System.out.println("s:" + s);

//        String s1 = AESUtil.encrypt(s, "1234");
//        System.out.println("s1:" + s1);

//        System.out.println("s2:"+AESUtil.decrypt(s1, "1234"));


    }

}