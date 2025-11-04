package com.zkrypto.zk_mpc_core.application.tss.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.ProcessGroup;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProtocolData {
    private ProcessGroup processGroup;
    private List<String> memberIds;
    private Integer threshold;
    private byte[] messageBytes;
    private String target;
    @Setter
    private List<String> participantIds;

    public ProtocolData(ProcessGroup processGroup, List<String> memberIds, Integer threshold, byte[] messageBytes, String target) {
        this.processGroup = processGroup;
        this.memberIds = memberIds;
        this.threshold = threshold;
        this.messageBytes = messageBytes;
        this.target = target;
        this.participantIds = memberIds;
    }
}
