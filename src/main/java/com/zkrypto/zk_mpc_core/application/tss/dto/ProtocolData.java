package com.zkrypto.zk_mpc_core.application.tss.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ProcessGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProtocolData {
    private List<String> memberIds;
    private Integer threshold;
    private byte[] messageBytes;
    private String target;
    private List<String> participantIds;

    public ProtocolData(List<String> memberIds, Integer threshold, byte[] messageBytes, String target) {
        this.memberIds = memberIds;
        this.threshold = threshold;
        this.messageBytes = messageBytes;
        this.target = target;
        this.participantIds = memberIds.stream().filter(id -> !id.equals(target)).toList();
    }
}
