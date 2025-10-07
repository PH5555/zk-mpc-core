package com.zkrypto.zk_mpc_core.application.tss.dto;

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
}
