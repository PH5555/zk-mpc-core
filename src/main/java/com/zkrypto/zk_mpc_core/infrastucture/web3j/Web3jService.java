package com.zkrypto.zk_mpc_core.infrastucture.web3j;

import com.zkrypto.zk_mpc_core.application.blockchain.BlockchainPort;
import org.springframework.stereotype.Component;

@Component
public class Web3jService implements BlockchainPort {
    @Override
    public void sendTransaction(byte[] message, String rHex, String sHex) {

    }
}
