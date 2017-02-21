package ru.inovus.egisz.medorg.util;

import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CryptoHelper {

    static final String GOST_EL_SIGN_NAME = "ECGOST3410";

     /**
     * Формирует хэш данных по ГОСТ 34.11 и кодирует его в base64
     *
     * @param data входные данные
     * @return хэш в base64
     */
    public static String getBase64Digest(String data) {
        GOST3411Digest digest = new GOST3411Digest();
        digest.update(data.getBytes(), 0, data.getBytes().length);
        byte[] resBuf = new byte[digest.getDigestSize()];
        digest.doFinal(resBuf, 0);
        return new String(Base64.getEncoder().encode(resBuf));
    }

    /**
     * Подписывает данные ЭЦП по ГОСТ 34.10 и кодирует ее в base64
     *
     * @param data входные данные
     * @param key закрытый ключ
     * @return подпись в base64
     * @throws GeneralSecurityException
     */
    public static String getSignature(String data, String key) throws GeneralSecurityException {

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));

        PrivateKey privateKey = KeyFactory.getInstance(GOST_EL_SIGN_NAME,  BouncyCastleProvider.PROVIDER_NAME).generatePrivate(privateKeySpec);

        Signature signature = Signature.getInstance(GOST_EL_SIGN_NAME, BouncyCastleProvider.PROVIDER_NAME);

        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signBytes = signature.sign();

        return new String(Base64.getEncoder().encode(signBytes));
    }

}