package com.zkrypto.zk_mpc_core.application.message.dto;

import lombok.Builder;

@Builder
public record MessageProcessEndEvent(
        String recipient,
        String message,
        String type,
        String sid
) {
}
