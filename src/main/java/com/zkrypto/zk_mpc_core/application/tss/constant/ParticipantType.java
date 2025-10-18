package com.zkrypto.zk_mpc_core.application.tss.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
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

    /**
     * 주어진 ProcessGroup에 해당하는 첫 번째 ParticipantType을 반환합니다.
     * enum에 정의된 순서를 기준으로 첫 번째 요소를 찾습니다.
     * @param process 찾고자 하는 프로세스 그룹
     * @return 해당 그룹의 첫 번째 ParticipantType
     * @throws IllegalArgumentException 주어진 그룹에 해당하는 타입이 없을 경우
     */
    public static ParticipantType getFirstStep(ProcessGroup process) {
        return Arrays.stream(values())
                .filter(type -> type.getProcessGroup() == process)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("group에 해당하는 participantType이 없습니다."));
    }

    public static ParticipantType of(String typeName) {
        return Arrays.stream(ParticipantType.values())
                .filter(type -> type.getTypeName().equals(typeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("participantType이 잘못됐습니다. :" + typeName));
    }
}

