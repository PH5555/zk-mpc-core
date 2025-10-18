package com.zkrypto.zk_mpc_core.common.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zkrypto.zk_mpc_core.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import com.zkrypto.zk_mpc_core.application.tss.dto.DelegateOutput;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class DelegateOutputDeserializer extends JsonDeserializer<DelegateOutput> {
    @Override
    public DelegateOutput deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException, JacksonException {
        // ObjectMapper를 가져와서 내부 파싱에 재사용합니다.
        ObjectMapper mapper = new ObjectMapper();

        // JSON을 트리 구조의 노드로 읽어옵니다.
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(p);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // 최상위 키(key) 이름을 가져옵니다. ("Continue" 또는 "Done")
        String typeKey = rootNode.fieldNames().next();
        if ("Continue".equals(typeKey)) {
            JsonNode continueNode = rootNode.get("Continue");
            // "Continue" 키의 값(JSON 배열)을 List<ContinueMessage>로 파싱합니다.
            List<ContinueMessage> messages = mapper.convertValue(continueNode, new TypeReference<>() {});
            return new DelegateOutput(DelegateOutputStatus.CONTINUE, messages, null);

        } else if ("Done".equals(typeKey)) {
            JsonNode doneNode = rootNode.get("Done");

            return new DelegateOutput(DelegateOutputStatus.DONE, null, doneNode.toString());
        }

        throw new RuntimeException("타입이 불일치합니다.");
    }
}