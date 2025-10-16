package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import lombok.Builder;

@Builder
public record ProceedRoundMessage(
        String type,
        String message,
        String sid
) {
}
