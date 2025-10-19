package com.zkrypto.zk_mpc_core.infrastucture.web.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import com.zkrypto.zk_mpc_core.application.tss.constant.ProcessGroup;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InitProtocolCommand(
        @NotNull ProcessGroup process,
        @NotNull String sid,
        @NotNull List<String> memberIds,
        @NotNull Integer n,
        byte[] messageBytes
) {
}
