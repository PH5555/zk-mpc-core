package com.zkrypto.zk_mpc_core.infrastucture.mpcRest;

import com.zkrypto.zk_mpc_core.application.mpcRest.MpcRestPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MpcRestTemplate implements MpcRestPort {
    private final RestTemplate restTemplate;

    private final String url = "https://localhost:8080/api/v1";

    @Override
    public void setAddress(String sid, String publicKey, String address) {

    }

    @Override
    public String getPublicKey() {
        return "";
    }
}
