package com.rsupport.mobile.agent.utils.crypto;

import com.rsupport.rscommon.define.RSErrorCode;
import com.rsupport.rscommon.exception.RSException;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.constant.GlobalResource;
import com.rsupport.util.log.RLog;

import static com.rsupport.rscommon.define.RSErrorCode.Runtime.CRYPTO_BAD_PADDING;
import static com.rsupport.rscommon.define.RSErrorCode.Runtime.CRYPTO_ILLEGAL_BLOCK_SIZE;
import static com.rsupport.rscommon.define.RSErrorCode.Runtime.CRYPTO_INVALID_ALGORITHM;
import static com.rsupport.rscommon.define.RSErrorCode.Runtime.CRYPTO_INVALID_KEY;

public class WebCrypto {

    private String algorithm;        //"AES/CBC/PKCS5Padding";
    private String keyText;            //"B0LPpnh15UuI+2wd";
    private String ivParameter;        //"r2uI+pMoXJDB/RxY";

    private Key key;
    private IvParameterSpec ivParameterSpec;
    private Cipher cipher;

    public WebCrypto() throws RSException {
        init();
    }

    private void init() throws RSException {
        getSignParam();
        try {
            key = generateKey("AES", keyText.getBytes());
            ivParameterSpec = new IvParameterSpec(ivParameter.getBytes());
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchPaddingException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Runtime.CRYPTO_BAD_PADDING);
        } catch (NoSuchAlgorithmException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Runtime.CRYPTO_INVALID_ALGORITHM);
        } catch (InvalidKeyException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Runtime.CRYPTO_INVALID_KEY);
        } catch (InvalidKeySpecException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Runtime.CRYPTO_INVALID_KEY);
        }

    }

    private void getSignParam() {
        algorithm = GlobalResource.algorithm;
        keyText = GlobalResource.keyText;
        ivParameter = GlobalResource.ivParameter;
    }

    private Key generateKey(String algorithm, byte[] keyData) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        algorithm = algorithm.toUpperCase();
        if ("DES".equals(algorithm)) {
            KeySpec keySpec = new DESKeySpec(keyData);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return secretKey;
        } else if ("DESEDE".equals(algorithm) || "TRIPLEDES".equals(algorithm)) {
            KeySpec keySpec = new DESedeKeySpec(keyData);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return secretKey;
        } else {
            SecretKeySpec keySpec = new SecretKeySpec(keyData, algorithm);
            return keySpec;
        }
    }

    public String encrypt(String plainText) throws RSException {
        if (plainText == null) {
            plainText = "";
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            byte[] plain = plainText.getBytes(EngineConfigSetting.UTF_8);
            byte[] encrypt = cipher.doFinal(plain);
            String encryptString = new String(Base64.encodeBase64(encrypt), EngineConfigSetting.UTF_8);
            plain = null;
            encrypt = null;
            return encryptString;
        } catch (BadPaddingException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_BAD_PADDING);
        } catch (InvalidKeyException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_INVALID_KEY);
        } catch (IllegalBlockSizeException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_ILLEGAL_BLOCK_SIZE);
        } catch (InvalidAlgorithmParameterException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_INVALID_ALGORITHM);
        }
    }

    public String decrypt(String encryptText) throws RSException {
        if (encryptText.length() <= 0) return "";
        try {
            if (encryptText.contains(" ")) {
                encryptText = encryptText.replace(" ", "+");
            }
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] encrypt = Base64.decodeBase64(encryptText.getBytes(EngineConfigSetting.UTF_8));
            byte[] plain = cipher.doFinal(encrypt);
            String decryptString = new String(plain, EngineConfigSetting.UTF_8);
            encrypt = null;
            plain = null;
            return decryptString;
        } catch (InvalidKeyException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_INVALID_KEY);
        } catch (InvalidAlgorithmParameterException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_INVALID_ALGORITHM);
        } catch (IllegalBlockSizeException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_ILLEGAL_BLOCK_SIZE);
        } catch (BadPaddingException e) {
            RLog.w(e);
            throw new RSException(CRYPTO_BAD_PADDING);
        }
    }

    public byte[] encryptAES(byte[] data) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public byte[] decryptAES(byte[] data) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
        return cipher.doFinal(data);
    }
}
