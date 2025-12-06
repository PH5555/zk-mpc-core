package com.zkrypto.zk_mpc_core.infrastucture.web3j;

import com.zkrypto.zk_mpc_core.application.blockchain.BlockchainPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class Web3jService implements BlockchainPort {
    private final Web3j web3j;

    private static final BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
    private static final BigInteger gasLimit = BigInteger.valueOf(21_000L);
    private static final long chainId = 1337;

    @Override
    public String sendTransaction(byte[] message, String rHex, String sHex, String publicKey, String nonce, String value, String toAddress) {
        BigInteger transferValue = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
        RawTransaction rawTransaction = RawTransaction.createTransaction(new BigInteger(nonce), gasPrice, gasLimit, toAddress, transferValue, "");
        byte[] v = BigInteger.valueOf(chainId).toByteArray();
        byte[] encodedTxForHashing = TransactionEncoder.encode(rawTransaction, new Sign.SignatureData(v, new byte[]{}, new byte[]{}));
        byte[] transactionHash = org.web3j.crypto.Hash.sha3(encodedTxForHashing);

        byte[] r = Numeric.hexStringToByteArray(rHex);
        byte[] s = Numeric.hexStringToByteArray(sHex);

        Integer correctRecId = recoverSignature(r, s, transactionHash, publicKey);
        String signedMessage = signTransaction(correctRecId, r, s, rawTransaction);

        try {
            return web3j.ethSendRawTransaction(signedMessage).send().getTransactionHash();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer recoverSignature(byte[] r, byte[] s, byte[] transactionHash, String publicKey) {
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        Integer correctRecId = null;

        for (int i = 0; i < 4; i++) {
            BigInteger recoveredPublicKey = Sign.recoverFromSignature(i, sig, transactionHash);
            if (recoveredPublicKey != null && publicKey.equals(recoveredPublicKey.toString(16).toUpperCase())) {
                correctRecId = i;
                break;
            }
        }

        if (correctRecId == null) {
            throw new IllegalArgumentException("Signature Verification FAILED. Aborting");
        }

        return correctRecId;
    }

    private String signTransaction(int correctRecId, byte[] r, byte[] s, RawTransaction rawTransaction) {
        long vLong = (chainId * 2) + 35 + correctRecId;
        byte[] v = BigInteger.valueOf(vLong).toByteArray();
        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
        byte[] signedMessage = TransactionEncoder.encode(rawTransaction, signatureData);
        return Numeric.toHexString(signedMessage);
    }
}
