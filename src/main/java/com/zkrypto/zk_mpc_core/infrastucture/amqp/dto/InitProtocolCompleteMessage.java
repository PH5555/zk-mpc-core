package com.zkrypto.zk_mpc_core.infrastucture.amqp.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record InitProtocolCompleteMessage(
        ParticipantType type,
        String sid,
        String memberId
) {
}
