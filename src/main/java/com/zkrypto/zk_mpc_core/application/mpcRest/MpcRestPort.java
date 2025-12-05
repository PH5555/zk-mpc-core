package com.zkrypto.zk_mpc_core.application.mpcRest;

import com.zkrypto.zk_mpc_core.infrastucture.mpcRest.dto.response.TransactionResponse;

public interface MpcRestPort {
    void setAddress(String sid, String publicKey, String address);
    String getPublicKey();
    TransactionResponse getLastTransaction(String sid);
}
