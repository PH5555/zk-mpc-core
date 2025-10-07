package com.zkrypto.zk_mpc_core.infrastucture.api;

import com.zkrypto.zk_mpc_core.application.group.port.out.GroupPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupWebClient implements GroupPort {
    @Override
    public int getGroupThreshold(String groupId) {
        return 0;
    }

    @Override
    public List<String> getGroupMemberIds(String groupId) {
        return List.of();
    }
}
