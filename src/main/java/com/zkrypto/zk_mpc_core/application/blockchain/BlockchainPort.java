package com.zkrypto.zk_mpc_core.application.blockchain;

public interface BlockchainPort {
    void sendTransaction(byte[] message, String rHex, String sHex, String publicKey, String nonce, String value, String toAddress);
}
