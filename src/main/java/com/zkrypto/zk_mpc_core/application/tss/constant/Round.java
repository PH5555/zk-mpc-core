package com.zkrypto.zk_mpc_core.application.tss.constant;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
public enum Round {
    // TShare rounds
    R2_PRIVATE_SHARE("R2PrivateShare"),
    R2_DECOMMIT("R2Decommit"),

    // TPresign rounds
    ROUND_ONE_BROADCAST("RoundOneBroadcast"),
    ROUND_ONE("RoundOne"),

    ROUND_TWO_BROADCAST("RoundTwoBroadcast"),
    ROUND_TWO("RoundTwo"),

    // another rounds
    ROUND_DEFAULT("RoundDefault");

    private final String name;

    /**
     * 현재 라운드의 다음 라운드를 반환합니다.
     * 마지막 라운드일 경우 null을 반환합니다.
     * @return 다음 Round 객체 또는 null
     */
    public Round getNextRound() {
        switch (this) {
            case R2_PRIVATE_SHARE:
                return R2_DECOMMIT;

            case ROUND_ONE_BROADCAST:
                return ROUND_ONE;

            case ROUND_TWO_BROADCAST:
                return ROUND_TWO;

            // 마지막 라운드인 경우
            case R2_DECOMMIT:
            case ROUND_ONE:
            case ROUND_TWO:
                return null;

            default:
                return null;
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
