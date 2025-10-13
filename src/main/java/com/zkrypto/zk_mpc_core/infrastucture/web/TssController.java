package com.zkrypto.zk_mpc_core.infrastucture.web;

import com.zkrypto.zk_mpc_core.application.tss.TssService;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitKeyShareProtocolCommand;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitProtocolCommand;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitSignProtocolCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tss")
@RequiredArgsConstructor
public class TssController {
    private final TssService tssService;

    @PostMapping("/init")
    public ResponseEntity<Void> initProtocol(InitProtocolCommand command) {
        tssService.initProtocol(command);
        return ResponseEntity.ok().build();
    }
}
