package com.zkrypto.zk_mpc_core.application.tss.constant;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public enum Round {
    // TShare rounds
    R2_PRIVATE_SHARE("R2PrivateShare", ParticipantType.TSHARE),
    R2_DECOMMIT("R2Decommit", ParticipantType.TSHARE),

    // TPresign rounds
    ROUND_ONE_BROADCAST("RoundOneBroadcast", ParticipantType.TPRESIGN),
    ROUND_ONE("RoundOne", ParticipantType.TPRESIGN),

    ROUND_TWO_BROADCAST("RoundTwoBroadcast", ParticipantType.TPRESIGN),
    ROUND_TWO("RoundTwo", ParticipantType.TPRESIGN),

    // another rounds
    ROUND_DEFAULT("RoundDefault", null);

    private final String name;
    private final ParticipantType type;

    /**
     * 현재 라운드의 다음 라운드를 반환합니다.
     * 마지막 라운드일 경우 null을 반환합니다.
     * @return 다음 Round 객체 또는 null
     */
    public Optional<Round> getNextRound() {
        switch (this) {
            case R2_PRIVATE_SHARE:
                return Optional.of(R2_DECOMMIT);

            case ROUND_ONE_BROADCAST:
                return Optional.of(ROUND_ONE);

            case ROUND_TWO_BROADCAST:
                return Optional.of(ROUND_TWO);

            // 마지막 라운드인 경우
            case R2_DECOMMIT:
            case ROUND_ONE:
            case ROUND_TWO:
                return Optional.empty();

            default:
                return Optional.empty();
        }
    }

    public static Round fromName(String name) {
        for (Round r : values()) {
            if (r.name.equals(name)) {
                return r;
            }
        }
        return Round.ROUND_DEFAULT;
    }

    public Boolean hasPendingPrerequisites() {
        switch (this) {
            case R2_DECOMMIT:
            case ROUND_ONE:
            case ROUND_TWO:
                return true;

            default:
                return false;
        }
    }
}
