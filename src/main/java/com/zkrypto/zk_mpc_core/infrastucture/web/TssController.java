package com.zkrypto.zk_mpc_core.infrastucture.web;

import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitProtocolCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tss")
public class TssController {
    @PostMapping("/init")
    public ResponseEntity<Void> initProtocol(InitProtocolCommand command) {
        // TODO: 각각의 멤버들에게 시작 메시지 전송
        return ResponseEntity.ok().build();
    }
}
