package com.zkrypto.zk_mpc_core.infrastucture.mpcRest.dto.response;

public record TransactionResponse(
        String transactionId,
        String nonce,
        String toAddress,
        String value
) {
}
