package com.zkrypto.zk_mpc_core.application.tss.dto;

import com.zkrypto.zk_mpc_core.application.tss.constant.Round;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@ToString
@Getter
public class ContinueMessage {
    private Map<String, String> message_type;
    private BigInteger identifier;
    private BigInteger from;
    private BigInteger to;
    private Boolean is_broadcast;
    private List<Integer> unverified_bytes;

    /**
     * 이 메시지가 어떤 라운드에 해당하는지 추출하여 반환합니다.
     * @return 해당하는 Round enum
     */
    public Round extractRound() {
        if (this.message_type == null || this.message_type.isEmpty()) {
            throw new RuntimeException("라운드를 찾을 수 없습니다.");
        }

        return this.message_type.values().stream()
                .findFirst()
                .map(Round::fromName)
                .orElseThrow(() -> new RuntimeException("라운드를 찾을 수 없습니다."));
    }
}
