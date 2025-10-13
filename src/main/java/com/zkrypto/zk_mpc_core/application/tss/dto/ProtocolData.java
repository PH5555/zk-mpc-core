package com.zkrypto.zk_mpc_core.application.tss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocolData {
    private List<String> memberIds;
    private Integer threshold;
    private byte[] messageBytes;
}
