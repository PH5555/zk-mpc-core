package com.zkrypto.zk_mpc_core.application.message;

public interface MessageBroker {
    void publish(String recipient, String message, String type, String sid);
}
