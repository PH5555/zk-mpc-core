package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

public record RoundCompleteMessage(
        String type,
        String roundName,
        String sid
) {
}
