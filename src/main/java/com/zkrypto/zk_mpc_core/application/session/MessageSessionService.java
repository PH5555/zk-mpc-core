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

    public void addSession(String key, ContinueMessage message) {
        session.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(message);
            return v;
        });
    }

    public void clearSession(String key) {
        session.remove(key);
    }

    public List<ContinueMessage> getSessionMessage(String key) {
        return session.getOrDefault(key, new ArrayList<>());
    }

    public int getSessionCount(String key) {
        return session.getOrDefault(key, new ArrayList<>()).size();
    }
}
