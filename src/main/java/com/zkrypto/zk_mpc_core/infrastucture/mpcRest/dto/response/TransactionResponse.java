package com.zkrypto.zk_mpc_core.infrastucture.mpcRest.dto.response;

public record TransactionResponse(
        String nonce,
        String toAddress,
        String value
) {
}
