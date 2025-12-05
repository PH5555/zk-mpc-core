package com.zkrypto.zk_mpc_core.application.mpcRest;

public interface MpcRestPort {
    void setAddress(String sid, String publicKey, String address);
    String getPublicKey();
}
