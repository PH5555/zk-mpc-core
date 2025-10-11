package com.zkrypto.zk_mpc_core.infrastucture.web.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;

import java.util.List;

public record InitProtocolCommand(
        String sid,
        ParticipantType type,
        List<String> memberIds
) {
}
