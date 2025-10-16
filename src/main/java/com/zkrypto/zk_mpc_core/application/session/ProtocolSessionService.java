package com.zkrypto.zk_mpc_core.application.session;

import com.zkrypto.zk_mpc_core.application.tss.dto.ProtocolData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProtocolSessionService {
    Map<String, ProtocolData> session = new ConcurrentHashMap<>();

    public void addSession(String groupId, ProtocolData protocolData) {
        session.compute(groupId, (k, v) -> protocolData);
    }

    public void clearSession(String groupId) {
        session.put(groupId, null);
    }

    public ProtocolData getSession(String groupId) {
        return session.getOrDefault(groupId, new ProtocolData());
    }
}
