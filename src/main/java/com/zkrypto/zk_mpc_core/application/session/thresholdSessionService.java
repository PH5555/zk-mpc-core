package com.zkrypto.zk_mpc_core.application.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class thresholdSessionService {
    Map<String, Integer> session = new ConcurrentHashMap<>();

    public Integer addSession(String key) {
        return session.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
    }

    public void clearSession(String key) {
        session.remove(key);
    }

    public int getSessionCount(String key) {
        return session.getOrDefault(key, 0);
    }
}
