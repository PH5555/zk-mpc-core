package com.zkrypto.zk_mpc_core.application.group.port.out;

import java.util.List;

public interface GroupPort {
    int getGroupThreshold(String groupId);
    List<String> getGroupMemberIds(String groupId);
}
