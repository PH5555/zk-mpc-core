package com.zkrypto.zk_mpc_core.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.util.concurrent.Executors;

@Configuration
public class Web3Config {
    @Value("${blockchain.url}")
    private String rpcUrl;

    @Bean
    public Web3j web3j() {
        long pollingInterval = 2000L;
        var executor = Executors.newScheduledThreadPool(1);
        return Web3j.build(new HttpService(rpcUrl), pollingInterval, executor);
    }

    @Bean
    public TransactionReceiptProcessor transactionReceiptProcessor(Web3j web3j) {
        int attempts = 40;
        long pollingInterval = 15000;

        return new PollingTransactionReceiptProcessor(
                web3j,
                pollingInterval,
                attempts
        );
    }
}