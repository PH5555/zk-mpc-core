package com.zkrypto.zk_mpc_core.application.session;

import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MessageSessionService {
    Map<String, List<ContinueMessage>> session = new ConcurrentHashMap<>();

    public void addSession(String groupId, String roundName, List<ContinueMessage> messages) {
        String sessionId = groupId.concat(roundName);
        session.compute(sessionId, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.addAll(messages);
            return v;
        });
        log.info("{} 메시지 세션 추가", sessionId);
    }

    public void clearSession(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        session.remove(sessionId);
        log.info("{} 메시지 세션 삭제", sessionId);
    }

    public List<ContinueMessage> getSessionMessage(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        return session.getOrDefault(sessionId, new ArrayList<>());
    }

    public int getSessionCount(String groupId, String roundName) {
        String sessionId = groupId.concat(roundName);
        int count = session.getOrDefault(sessionId, new ArrayList<>()).size();
        log.info("{} 메시지 세션 갯수: {}", sessionId, count);
        return count;
    }
}
