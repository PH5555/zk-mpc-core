package com.zkrypto.zk_mpc_core.application.message.dto;

import com.zkrypto.constant.ParticipantType;
import lombok.Builder;

@Builder
public record InitProtocolEvent(
        String recipient,
        ParticipantType participantType,
        String sid,
        String[] otherIds,
        String[] participantIds,
        Integer threshold,
        byte[] messageBytes,
        String target,
        Boolean isRestart
) {
}
