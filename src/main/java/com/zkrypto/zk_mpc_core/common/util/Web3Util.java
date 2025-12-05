package com.zkrypto.zk_mpc_core.common.util;

import com.zkrypto.cryptolib.TssBridge;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Web3Util {
    public static String recoverPublicKey(String message) {
        String masterKey = TssBridge.getMasterKey(message);
        String rawHex = masterKey.replaceAll("\"", "");
        if (rawHex.length() > 130) {
            rawHex = rawHex.substring(rawHex.length() - 130);
        }
        String publicKey = rawHex;
        if (publicKey.startsWith("04") && publicKey.length() == 130) {
            publicKey = publicKey.substring(2);
        }
        return publicKey;
    }
}
