package com.zkrypto.zk_mpc_core.application.tss;

import com.zkrypto.zk_mpc_core.application.group.port.out.GroupPort;
import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.session.SessionService;
import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import com.zkrypto.zk_mpc_core.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TssService {
    private final MessageBroker tssMessageBroker;
    private final SessionService sessionService;
    private final GroupPort groupPort;

    /**
     * 라운드 메시지가 모두 모이면 전송하는 메서드입니다.
     * @param type 메시지 타입
     * @param message 메시지 내용
     * @param sid 그룹 id
     */
    public void collectMessageAndCheckCompletion(String type, String message, String sid) {
        // ContinueMessage 파싱
        ContinueMessage continueMessage = (ContinueMessage)JsonUtil.parse(message, ContinueMessage.class);

        // ContinueMessage 에서 라운드 정보 추출
        String roundName = continueMessage.getMessage_type().keySet().stream().findFirst().get();

        // 세션에 현재 메시지 추가
        sessionService.addSession(sid, roundName, continueMessage);

        // 현재 그룹의 임계치 조회
        int threshold = groupPort.getGroupThreshold(sid);

        // 임계치보다 현재 라운드의 메시지가 많아지면 메시지 전송
        if(sessionService.getSessionCount(sid, roundName) >= threshold) {
            sendAllMessages(sessionService.getSessionMessage(sid, roundName), type, sid);
        }
    }

    public void checkInitProtocolStatus(String sid, ParticipantType type) {

    }

    private void sendAllMessages(List<ContinueMessage> continueMessages, String type, String sid) {
        // broadcast 먼저 처리하도록 정렬
        log.info("broad cast 정렬");
        continueMessages.sort(Comparator.comparing(ContinueMessage::getIs_broadcast).reversed());

        // 메시지 목록을 순회하며 각 메시지를 처리
        log.info("메시지 목록 순회 시작");
        continueMessages.forEach(message -> processAndSendMessage(message, type, sid));
    }

    private void processAndSendMessage(ContinueMessage message, String type, String sid) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? groupPort.getGroupMemberIds(sid) // Is_broadcast이면 모든 참여자
                : List.of(message.getTo().toString()); // 아니면 한명

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            // TODO: 자신은 제외하는 로직 추가
            log.info("메시지 전송 to :" + recipient + " message: " + JsonUtil.toString(message).substring(0, 30) + "...");
            tssMessageBroker.publish(recipient, JsonUtil.toString(message), type, sid);
        });
    }
}
