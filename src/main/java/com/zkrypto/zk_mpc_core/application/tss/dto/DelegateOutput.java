package com.zkrypto.zk_mpc_core.application.tss.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zkrypto.zk_mpc_core.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zk_mpc_core.common.serializer.DelegateOutputDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@JsonDeserialize(using = DelegateOutputDeserializer.class)
public class DelegateOutput {
    private DelegateOutputStatus delegateOutputStatus;
    private List<ContinueMessage> continueMessages;
    private Object doneMessage;
}