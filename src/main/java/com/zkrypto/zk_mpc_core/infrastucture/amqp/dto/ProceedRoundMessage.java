package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record ProceedRoundMessage(
        ParticipantType type,
        String message,
        String sid
) {
}
