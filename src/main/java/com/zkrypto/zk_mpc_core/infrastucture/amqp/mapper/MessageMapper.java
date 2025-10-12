package com.zkrypto.zk_mpc_core.infrastucture.amqp.mapper;

import com.zkrypto.zk_mpc_core.application.message.dto.InitKeyShareProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitSignProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.InitProtocolMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.StartProtocolMessage;

public class MessageMapper {
    public static ProceedRoundMessage from(MessageProcessEndEvent event) {
        return ProceedRoundMessage.builder()
                .type(event.type())
                .message(event.message())
                .sid(event.sid())
                .build();
    }

    public static StartProtocolMessage from(InitProtocolEndEvent event) {
        return StartProtocolMessage.builder()
                .type(event.type())
                .sid(event.sid())
                .build();
    }

    public static InitProtocolMessage from(InitKeyShareProtocolEvent event) {
        return InitProtocolMessage.builder()
                .participantType(event.participantType())
                .sid(event.sid())
                .otherIds(event.otherIds())
                .threshold(event.threshold())
                .build();
    }

    public static InitProtocolMessage from(InitSignProtocolEvent event) {
        return InitProtocolMessage.builder()
                .participantType(event.participantType())
                .sid(event.sid())
                .otherIds(event.otherIds())
                .threshold(event.threshold())
                .messageBytes(event.messageBytes())
                .build();
    }
}
