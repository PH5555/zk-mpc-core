package com.zkrypto.zk_mpc_core.infrastucture.amqp.mapper;

import com.zkrypto.dto.InitProtocolMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.StartProtocolMessage;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;

public class MessageMapper {
    public static ProceedRoundMessage from(MessageProcessEndEvent event) {
        return new ProceedRoundMessage(event.type(), event.message(), event.sid());
    }

    public static StartProtocolMessage from(InitProtocolEndEvent event) {
        return new StartProtocolMessage(event.type(), event.sid());
    }

    public static InitProtocolMessage from(InitProtocolEvent event) {
        return new InitProtocolMessage(
                event.participantType(),
                event.sid(),
                event.otherIds(),
                event.participantIds(),
                event.threshold(),
                event.messageBytes(),
                event.target(),
                event.isRestart()
        );
    }
}
