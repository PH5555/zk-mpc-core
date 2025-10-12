package com.zkrypto.zk_mpc_core.application.tss.constant;

public enum ProcessGroup {
    KEY_GENERATION, // AUXINFO, TSHARE 가 속할 키 생성 프로세스
    SIGNING,        // TPRESIGN, SIGN 이 속할 서명 프로세스
    REFRESH
}
