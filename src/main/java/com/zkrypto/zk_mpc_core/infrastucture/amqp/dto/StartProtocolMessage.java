package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record StartProtocolMessage(
        ParticipantType type,
        String sid
) {
}
