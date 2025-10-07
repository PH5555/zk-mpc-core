package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import lombok.Builder;

@Builder
public record ProceedRoundCommand(
        String type,
        String message,
        String sid
) {
}
