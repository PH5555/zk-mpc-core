package com.zkrypto.zk_mpc_core.application.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FactorySessionService {
    Map<String, Set<String>> session = new ConcurrentHashMap<>();

    public void addSession(String groupId, String memberId) {
        session.compute(groupId, (k, v) -> (v == null) ? new HashSet<>() : v).add(memberId);
        log.info("{} 팩토리 세션 추가", groupId);
    }

    public List<String> getAllSession(String groupId) {
        return session.getOrDefault(groupId, new HashSet<>()).stream().toList();
    }

    public void clearSession(String groupId) {
        session.put(groupId, null);
        log.info("{} 팩토리 세션 삭제", groupId);
    }

    public int getSessionCount(String groupId) {
        int count = session.getOrDefault(groupId, new HashSet<>()).size();
        log.info("{} 팩토리 세션 갯수: {}", groupId, count);
        return count;
    }
}
