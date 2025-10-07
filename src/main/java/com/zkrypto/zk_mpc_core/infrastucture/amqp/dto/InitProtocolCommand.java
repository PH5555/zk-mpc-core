package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;

public record InitProtocolCommand(
        String sid,
        ParticipantType type
) {
}
