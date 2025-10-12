package com.zkrypto.zk_mpc_core.application.tss.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ParticipantType {

    // 1. KEY_GENERATION 프로세스 그룹
    AUXINFO("AuxInfo", ProcessGroup.KEY_GENERATION),
    TSHARE("TShare", ProcessGroup.KEY_GENERATION),

    // 2. REFRESH 프로세스 그룹
    TREFRESH("TRefresh", ProcessGroup.REFRESH),

    // 3. SIGNING 프로세스 그룹
    TPRESIGN("TPreSign", ProcessGroup.SIGNING),
    SIGN("Sign", ProcessGroup.SIGNING);

    private final String typeName;
    private final ProcessGroup processGroup; // 어떤 프로세스 그룹에 속하는지 명시

    /**
     * 현재 단계가 속한 프로세스 내에서 다음 단계를 반환합니다.
     * 프로세스의 마지막 단계이거나 단일 단계 프로세스일 경우 Optional.empty()를 반환합니다.
     * @return Optional<ParticipantType> 다음 단계
     */
    public Optional<ParticipantType> getNextStep() {
        return switch (this) {
            case AUXINFO -> Optional.of(TSHARE);
            case TPRESIGN -> Optional.of(SIGN);
            // TSHARE, SIGN, TREFRESH 는 각 프로세스의 마지막 단계이므로 다음이 없음
            default -> Optional.empty();
        };
    }
}

