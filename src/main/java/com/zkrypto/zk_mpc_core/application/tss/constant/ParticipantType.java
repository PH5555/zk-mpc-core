package com.zkrypto.zk_mpc_core.application.tss.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ParticipantType {
    AUXINFO("AuxInfo"),
    TSHARE("TShare"),
    TREFRESH("TRefresh"),
    TPRESIGN("TPreSign"),
    SIGN("Sign");

    private String typeName;
}

