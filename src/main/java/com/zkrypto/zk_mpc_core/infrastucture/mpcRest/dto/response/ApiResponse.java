package com.zkrypto.zk_mpc_core.infrastucture.mpcRest.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final String message;
    private final T data;
}
