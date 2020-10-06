package com.rsupport.mobile.agent.utils.crypto;


public class SeedCrypto {

    public static String encryptKey;    //"isike%ou+lovey&uihatey=u";


    public static String encrypt(String plainText) {
        Base64Utils base64 = new Base64Utils();
        return base64.encrypt(plainText, encryptKey);
    }

    public static String decrypt(String cipher) {
        Base64Utils base64 = new Base64Utils();
        return base64.decrypt(cipher, encryptKey);
    }
}
