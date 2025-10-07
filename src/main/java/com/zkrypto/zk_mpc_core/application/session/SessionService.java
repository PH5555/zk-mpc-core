package com.zkrypto.zk_mpc_core.application.session;

import com.rabbitmq.client.ChannelContinuationTimeoutException;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SessionService {
    Map<String, List<ContinueMessage>> session = new ConcurrentHashMap<>();

    public void addSession(String groupId, String roundName, ContinueMessage message) {
        String sessionId = groupId.concat(roundName);
        session.compute(sessionId, (k, v) -> (v == null) ? new ArrayList<>() : v).add(message);
        log.info("{} 세션 추가 : {}", sessionId, session.get(sessionId).size());
    }

    public void clearSession(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        session.put(sessionId, null);
        log.info("{} 세션 삭제", sessionId);
    }

    public List<ContinueMessage> getSessionMessage(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        return session.getOrDefault(sessionId, new ArrayList<>());
    }

    public int getSessionCount(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        return session.getOrDefault(sessionId, new ArrayList<>()).size();
    }
}
