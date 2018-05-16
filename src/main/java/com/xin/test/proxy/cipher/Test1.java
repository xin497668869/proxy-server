package com.xin.test.proxy.cipher;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.util.Enumeration;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Test1 {
    public static void main(String[] args) throws Exception {

        // Dynamically register the SunMSCAPI provider
        Security.addProvider(new sun.security.mscapi.SunMSCAPI());


        Provider p = Security.getProvider("SunMSCAPI");

        System.out.println("SunMSCAPI provider classname is " +
                                   p.getClass().getName());
        System.out.println("SunMSCAPI provider name is " + p.getName());
        System.out.println("SunMSCAPI provider version # is " + p.getVersion());
        System.out.println("SunMSCAPI provider info is " + p.getInfo());

        /*
         * Secure Random
         */
        SecureRandom random = SecureRandom.getInstance("Windows-PRNG", p);
        System.out.println("    Windows-PRNG is implemented by: " +
                                   random.getClass().getName());

        /*
         * Key Store
         */
        KeyStore keystore = KeyStore.getInstance("Windows-MY", p);


        test(keystore);
        System.out.println("    Windows-MY is implemented by: " +
                                   keystore.getClass().getName());

        keystore = KeyStore.getInstance("Windows-ROOT", p);
        System.out.println("    Windows-ROOT is implemented by: " +
                                   keystore.getClass().getName());
        test(keystore);
        /*
         * Signature
         */
        Signature signature = Signature.getInstance("SHA1withRSA", p);
        System.out.println("    SHA1withRSA is implemented by: " +
                                   signature.getClass().getName());

        signature = Signature.getInstance("MD5withRSA", p);
        System.out.println("    MD5withRSA is implemented by: " +
                                   signature.getClass().getName());

        signature = Signature.getInstance("MD2withRSA", p);
        System.out.println("    MD2withRSA is implemented by: " +
                                   signature.getClass().getName());

        /*
         * Key Pair Generator
         */
        KeyPairGenerator keypairGenerator =
                KeyPairGenerator.getInstance("RSA", p);
        System.out.println("    RSA is implemented by: " +
                                   keypairGenerator.getClass().getName());

        /*
         * Cipher
         */
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance("RSA", p);
            System.out.println("    RSA is implemented by: " +
                                       cipher.getClass().getName());

            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", p);
            System.out.println("    RSA/ECB/PKCS1Padding is implemented by: " +
                                       cipher.getClass().getName());

        } catch (GeneralSecurityException e) {
            System.out.println("Cipher not supported by provider, skipping...");
        }
    }

    private static void test(KeyStore keystore) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keystore.load(null, null);
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            System.out.println(keystore.getCertificate(aliases.nextElement()));
        }
    }
}
