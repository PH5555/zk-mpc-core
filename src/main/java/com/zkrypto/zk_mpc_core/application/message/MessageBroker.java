package com.zkrypto.zk_mpc_core.application.message;

import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;

public interface MessageBroker {
    void publish(MessageProcessEndEvent event);
    void publish(InitProtocolEndEvent event);
    void publish(InitProtocolEvent event);
}
