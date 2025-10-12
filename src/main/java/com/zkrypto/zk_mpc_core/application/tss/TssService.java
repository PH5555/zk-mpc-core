package com.zkrypto.zk_mpc_core.application.tss;

import com.zkrypto.zk_mpc_core.application.group.port.out.GroupPort;
import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitKeyShareProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.application.session.FactorySessionService;
import com.zkrypto.zk_mpc_core.application.session.MessageSessionService;
import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.common.util.JsonUtil;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitProtocolCommand;
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
    private final MessageSessionService messageSessionService;
    private final FactorySessionService factorySessionService;
    private final GroupPort groupPort;

    /**
     * 라운드 메시지 상태를 확인하고 임계치를 만족하면 메시지를 처리하는 메서드입니다.
     * @param type 프로토콜 타입
     * @param message 메시지 내용
     * @param sid 그룹 id
     */
    public void collectMessageAndCheckCompletion(ParticipantType type, String message, String sid) {
        // ContinueMessage 파싱
        ContinueMessage continueMessage = (ContinueMessage)JsonUtil.parse(message, ContinueMessage.class);

        // ContinueMessage 에서 라운드 정보 추출
        String roundName = continueMessage.getMessage_type().keySet().stream().findFirst().get();

        // 세션에 현재 메시지 추가
        messageSessionService.addSession(sid, roundName, continueMessage);

        // 현재 그룹의 임계치 조회
        int threshold = groupPort.getGroupThreshold(sid);

        // 임계치보다 현재 라운드의 메시지가 많아지면 메시지 전송
        if(messageSessionService.getSessionCount(sid, roundName) >= threshold) {
            List<ContinueMessage> currentRoundMessages = messageSessionService.getSessionMessage(sid, roundName);
            sendAllMessages(currentRoundMessages, type, sid);
        }
    }

    /**
     * 프로토콜 참여자들의 상태를 확인하는 메서드 입니다.
     * 참여자가 모두 팩토리를 생성하면 프로토콜을 시작합니다.
     * @param sid 그룹 id
     * @param memberId 멤버 id
     * @param type 프로토콜 타입
     */
    public void checkInitProtocolStatus(String sid, String memberId, ParticipantType type) {
        // 팩토리 세션에 추가
        factorySessionService.addSession(sid, memberId);

        // 현재 그룹의 임계치 조회
        int threshold = groupPort.getGroupThreshold(sid);

        // 임계치보다 팩토리 생성자가 많아지면 프로토콜 시작 메시지 전송
        if(factorySessionService.getSessionCount(sid) >= threshold) {
            InitProtocolEndEvent event = InitProtocolEndEvent.builder().sid(sid).type(type).build();
            tssMessageBroker.publish(event);
        }
    }

    /**
     * 현재 라운드의 모든 메시지를 전송하는 메서드입니다.
     * 메시지의 내용에 따라 broadcast를 할 수도 있고 특정 사람에게만 보낼 수도 있습니다.
     * 메시지는 단일 전송 메시지보다
     * @param continueMessages 메시지 리스트
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     */
    private void sendAllMessages(List<ContinueMessage> continueMessages, ParticipantType type, String sid) {
        // broadcast 먼저 처리하도록 정렬
        log.info("broadcast 정렬");
        continueMessages.sort(Comparator.comparing(ContinueMessage::getIs_broadcast).reversed());

        // 메시지 목록을 순회하며 각 메시지를 처리
        log.info("메시지 목록 순회 시작");
        continueMessages.forEach(message -> processAndSendMessage(message, type, sid));
    }

    /**
     * 단일 메시지 처리 메서드입니다.
     * broadcast 여부에 따라 메시지 수신자를 결정하고 전송합니다.
     * @param message 메시지
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     */
    private void processAndSendMessage(ContinueMessage message, ParticipantType type, String sid) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? groupPort.getGroupMemberIds(sid).stream().filter(mid -> !mid.equals(message.getFrom().toString())).toList() // Is_broadcast이면 보내는 사람을 제외한 모든 참여자
                : List.of(message.getTo().toString()); // Is_broadcast가 false이면 한명

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            log.info("메시지 전송 to :" + recipient + " message: " + JsonUtil.toString(message).substring(0, 30) + "...");
            MessageProcessEndEvent event = MessageProcessEndEvent.builder().recipient(recipient).message(JsonUtil.toString(message)).type(type).sid(sid).build();
            tssMessageBroker.publish(event);
        });
    }

    /**
     * 프로토콜을 시작하는 메서드입니다.
     * 프로토콜 참여자 모두에게 시작 메시지를 전송합니다.
     * @param command
     */
    public void initKeyShareProtocol(InitProtocolCommand command) {
        command.memberIds().forEach(recipient -> {
            String[] otherIds = (String[])command.memberIds().stream().filter(id -> !id.equals(recipient)).toList().toArray();
            InitKeyShareProtocolEvent event = InitKeyShareProtocolEvent.builder().participantType(command.type()).sid(command.sid()).otherIds(otherIds).build();
            tssMessageBroker.publish(event);
        });
    }
}
