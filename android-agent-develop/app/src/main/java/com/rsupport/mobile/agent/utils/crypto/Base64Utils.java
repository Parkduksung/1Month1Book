package com.rsupport.mobile.agent.utils.crypto;

import org.apache.commons.codec.binary.Base64;

import config.EngineConfigSetting;


public class Base64Utils {


    //Base64 + Seed 암호화
    public String encrypt(String str, String key) {
        if (key.length() != 24) {
            return "";
        }
        try {
            String strResult = "";
            String strTemp;
            SeedAlg seedAlg = new SeedAlg(key.getBytes(EngineConfigSetting.UTF_8));
            strTemp = new String(Base64.encodeBase64(seedAlg.encrypt(str.getBytes(EngineConfigSetting.UTF_8))));
            for (int i = 0; i < strTemp.length(); i++) {
                if (strTemp.charAt(i) != '\n' && strTemp.charAt(i) != '\r') {
                    strResult = strResult + strTemp.charAt(i);
                }
            }
            return strResult;
        } catch (Exception ex) {
            return null;
        }
    }

    //Base64 + Seed 복호화
    public String decrypt(String str, String key) {
        if (key.length() != 24) {
            return "";
        }
        try {
            String strResult = "";
            String strTemp;
//			BASE64Decoder decoder = new BASE64Decoder();
            SeedAlg seedAlg = new SeedAlg(key.getBytes(EngineConfigSetting.UTF_8));
            strTemp = new String(seedAlg.decrypt(Base64.decodeBase64(str.getBytes(EngineConfigSetting.UTF_8))));
            for (int i = 0; i < strTemp.length() && strTemp.charAt(i) != 0; ) {
                if (strTemp.charAt(i) != '\n' && strTemp.charAt(i) != '\r') {
                    strResult = strResult + strTemp.charAt(i);
                    i++;
                }
            }
            return strResult;
        } catch (Exception ex) {
            return null;
        }
    }
}
