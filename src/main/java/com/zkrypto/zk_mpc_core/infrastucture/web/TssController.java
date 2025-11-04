package com.zkrypto.zk_mpc_core.infrastucture.web;

import com.zkrypto.zk_mpc_core.application.tss.TssService;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitProtocolCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/tss")
@RequiredArgsConstructor
public class TssController {
    private final TssService tssService;

    @PostMapping("/start")
    public ResponseEntity<Void> startProtocol(@RequestBody InitProtocolCommand command) {
        log.info("{} process 시작", command.process());
        tssService.initProtocol(command);
        return ResponseEntity.ok().build();
    }
}
