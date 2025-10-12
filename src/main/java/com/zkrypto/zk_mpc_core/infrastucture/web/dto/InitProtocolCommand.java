package com.zkrypto.zk_mpc_core.infrastucture.web.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InitProtocolCommand(
        @NotNull ParticipantType type,
        @NotNull String sid,
        @NotNull List<String> memberIds
) {
}
