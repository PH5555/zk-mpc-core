package com.zkrypto.zk_mpc_core.application.message.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record InitKeyShareProtocolEvent(
        String recipient,
        ParticipantType participantType,
        String sid,
        String[] otherIds
) {
}
