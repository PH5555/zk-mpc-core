package com.zkrypto.zk_mpc_core.application.tss;

import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.application.session.CompleteStatusSessionService;
import com.zkrypto.zk_mpc_core.application.session.FactorySessionService;
import com.zkrypto.zk_mpc_core.application.session.MessageSessionService;
import com.zkrypto.zk_mpc_core.application.session.ProtocolSessionService;
import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.tss.dto.ProtocolData;
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
    private final CompleteStatusSessionService completeStatusSessionService;
    private final ProtocolSessionService protocolSessionService;

    /**
     * 라운드 메시지 상태를 확인하고 임계치를 만족하면 메시지를 처리하는 메서드입니다.
     * @param type 프로토콜 타입
     * @param message 메시지 내용
     * @param sid 그룹 id
     */
    public void collectMessageAndCheckCompletion(ParticipantType type, String message, String sid) {
        log.info("라운드 메시지 수신");

        // ContinueMessage 파싱
        ContinueMessage continueMessage = (ContinueMessage)JsonUtil.parse(message, ContinueMessage.class);

        // ContinueMessage 에서 라운드 정보 추출
        String roundName = continueMessage.getMessage_type().keySet().stream().findFirst().get();

        // 세션에 현재 메시지 추가
        messageSessionService.addSession(sid, roundName, continueMessage);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);

        // 라운드 종료 클라이언트 수 조회
        int currentCount = messageSessionService.getSessionCount(sid, roundName);

        log.info("임계치: {}, 현재 수: {}", protocolData.getThreshold(), currentCount);

        // 임계치보다 현재 라운드의 메시지가 많아지면 메시지 전송
        if(currentCount >= protocolData.getThreshold()) {
            log.info("다음 라운드 진행 메시지 전송");
            List<ContinueMessage> currentRoundMessages = messageSessionService.getSessionMessage(sid, roundName);
            sendAllMessages(currentRoundMessages, type, sid, protocolData);
        }
    }

    /**
     * 프로토콜 참여자들의 초기화 상태를 확인하는 메서드 입니다.
     * 참여자가 모두 초기화를 완료하면 프로토콜을 시작합니다.
     * @param sid 그룹 id
     * @param memberId 멤버 id
     * @param type 프로토콜 타입
     */
    public void checkInitProtocolStatus(String sid, String memberId, ParticipantType type) {
        // 팩토리 세션에 추가
        log.info("프로토콜 초기화 종료 메시지 받음: {}", memberId);
        factorySessionService.addSession(sid, memberId);

        // 현재 그룹의 프로토콜 정보 조회
        int threshold = protocolSessionService.getSession(sid).getThreshold();

        // 초기화 완료한 클라이언트 수
        int currentCount = factorySessionService.getSessionCount(sid);

        log.info("임계치: {}, 초기화 완료 수: {}", threshold, currentCount);

        // 임계치보다 초기화 완료 클라이언트 수가 많아지면 프로토콜 시작 메시지 전송
        if(currentCount >= threshold) {
            log.info("{} 프로토콜 시작", type.getTypeName());
            factorySessionService.getAllSession(sid).forEach(recipient -> {
                log.info("프로토콜 시작 메시지 전송: {}", recipient);
                InitProtocolEndEvent event = InitProtocolEndEvent.builder()
                        .sid(sid)
                        .type(type)
                        .recipient(recipient)
                        .build();
                tssMessageBroker.publish(event);
            });
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
    private void sendAllMessages(List<ContinueMessage> continueMessages, ParticipantType type, String sid, ProtocolData protocolData) {
        // broadcast 먼저 처리하도록 정렬
        log.info("broadcast 정렬");
        continueMessages.sort(Comparator.comparing(ContinueMessage::getIs_broadcast).reversed());

        // 메시지 목록을 순회하며 각 메시지를 처리
        log.info("메시지 목록 순회 시작");
        continueMessages.forEach(message -> processAndSendMessage(message, type, sid, protocolData));
    }

    /**
     * 단일 메시지 처리 메서드입니다.
     * broadcast 여부에 따라 메시지 수신자를 결정하고 전송합니다.
     * @param message 메시지
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     */
    private void processAndSendMessage(ContinueMessage message, ParticipantType type, String sid, ProtocolData protocolData) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? protocolData.getMemberIds().stream().filter(mid -> !mid.equals(message.getFrom().toString())).toList() // Is_broadcast이면 보내는 사람을 제외한 모든 참여자
                : List.of(message.getTo().toString()); // Is_broadcast가 false이면 한명

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            log.info("메시지 전송 to :" + recipient + " message: " + JsonUtil.toString(message).substring(0, 30) + "...");
            MessageProcessEndEvent event = MessageProcessEndEvent.builder()
                    .recipient(recipient)
                    .message(JsonUtil.toString(message))
                    .type(type)
                    .sid(sid)
                    .build();
            tssMessageBroker.publish(event);
        });
    }

    /**
     * 프로토콜 종료 상태를 확인하는 메서드입니다.
     * 프로토콜 참여자 모두가 종료 상태이면 다음 프로토콜을 진행합니다.
     * @param sid
     * @param memberId
     * @param type
     */
    public void checkProtocolCompleteStatus(String sid, String memberId, ParticipantType type) {
        log.info("프로토콜 종료 메시지 수신");

        // 종료 상태 세션에 추가
        completeStatusSessionService.addSession(sid, memberId);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);

        // 프로토콜 종료 클라이언트 수 조회
        int currentCount = factorySessionService.getSessionCount(sid);

        log.info("임계치: {}, 프로토콜 종료 수: {}", protocolData.getThreshold(), currentCount);

        // 임계치보다 종료 상태 세션수가 많아지고 다음 프로토콜이 존재하면 시작 메시지 전송
        // 다음 프로토콜 존재하지 않으면 완전 종료
        // TODO: 프로토콜에 따라서 다음 과정 진행
        if(currentCount >= protocolData.getThreshold()) {
            type.getNextStep().ifPresentOrElse(
                    nextType -> sendInitMessage(protocolData.getMemberIds(), type, sid, protocolData.getThreshold(), protocolData.getMessageBytes()),
                    () -> {
                        log.info("완전 종료");
                    });
        }
    }

    /**
     * 프로세스 그룹에 따라서 프로토콜을 시작하는 메서드입니다.
     * @param command
     */
    public void startProtocol(InitProtocolCommand command) {
        // 실행해야하는 첫번째 프로토콜 조회
        ParticipantType participantType = ParticipantType.getFirstStep(command.process());

        // 프로토콜 데이터 저장
        ProtocolData protocolData = new ProtocolData(command.memberIds(), command.threshold(), command.messageBytes());
        protocolSessionService.addSession(command.sid(), protocolData);

        // 실행 메시지 전송
        sendInitMessage(command.memberIds(), participantType, command.sid(), command.threshold(), command.messageBytes());
    }

    /**
     * 프로토콜 초기화 메시지를 전송하는 메서드입니다.
     * @param memberIds 프로토콜 참여자 id
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     * @param threshold 임계치
     * @param messageBytes 메시지
     */
    private void sendInitMessage(List<String> memberIds, ParticipantType type, String sid, Integer threshold, byte[] messageBytes) {
        memberIds.forEach(recipient -> {
            // 해당 메시지를 받는 사람을 제외한 otherIds 생성
            String[] otherIds = (String[])memberIds.stream().filter(id -> !id.equals(recipient)).toList().toArray();

            // 메시지 전송
            InitProtocolEvent event = InitProtocolEvent.builder()
                    .participantType(type)
                    .sid(sid)
                    .otherIds(otherIds)
                    .threshold(threshold)
                    .messageBytes(messageBytes)
                    .recipient(recipient)
                    .build();
            log.info("{} 프로토콜 초기화 메시지 전송 : {}", type, recipient);
            tssMessageBroker.publish(event);
        });
    }
}
