package com.zkrypto.zk_mpc_core.application.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class thresholdSessionService {
    Map<String, Integer> session = new ConcurrentHashMap<>();

    public void addSession(String groupId) {
        session.compute(groupId, (k, v) -> (v == null) ? 1 : v + 1);
        log.info("{} 상태 세션 추가", groupId);
    }

    public void clearSession(String groupId) {
        session.remove(groupId);
        log.info("{} 상태 세션 삭제", groupId);
    }

    public int getSessionCount(String groupId) {
        int count = session.getOrDefault(groupId, 0);
        log.info("{} 상태 세션 갯수: {}", groupId, count);
        return count;
    }
}
