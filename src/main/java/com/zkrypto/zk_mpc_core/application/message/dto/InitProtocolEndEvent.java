package com.zkrypto.zk_mpc_core.application.message.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record InitProtocolEndEvent(
        String sid,
        ParticipantType type,
        @NotNull String recipient
) {
}
