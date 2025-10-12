package com.zkrypto.zk_mpc_core.application.message;

import com.zkrypto.zk_mpc_core.application.message.dto.InitKeyShareProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitSignProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;

public interface MessageBroker {
    void publish(MessageProcessEndEvent event);
    void publish(InitProtocolEndEvent event);
    void publish(InitKeyShareProtocolEvent event);
    void publish(InitSignProtocolEvent event);
}
