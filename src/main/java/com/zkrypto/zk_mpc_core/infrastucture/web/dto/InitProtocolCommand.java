package com.zkrypto.zk_mpc_core.infrastucture.web.dto;

import com.zkrypto.constant.ProcessGroup;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InitProtocolCommand(
        @NotNull ProcessGroup process,
        @NotNull String sid,
        @NotNull List<String> memberIds,
        @NotNull Integer threshold,
        String target,
        byte[] messageBytes
) {
}
