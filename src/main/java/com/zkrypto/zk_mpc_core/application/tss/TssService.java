package com.zkrypto.zk_mpc_core.application.tss;

import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.application.session.thresholdSessionService;
import com.zkrypto.zk_mpc_core.application.session.MessageSessionService;
import com.zkrypto.zk_mpc_core.application.session.ProtocolSessionService;
import com.zkrypto.zk_mpc_core.application.tss.constant.ParticipantType;
import com.zkrypto.zk_mpc_core.application.tss.constant.Round;
import com.zkrypto.zk_mpc_core.application.tss.dto.ContinueMessage;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.tss.dto.DelegateOutput;
import com.zkrypto.zk_mpc_core.application.tss.dto.ProtocolData;
import com.zkrypto.zk_mpc_core.common.util.JsonUtil;
import com.zkrypto.zk_mpc_core.infrastucture.web.dto.InitProtocolCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TssService {
    private final MessageBroker tssMessageBroker;
    private final MessageSessionService messageSessionService;
    private final thresholdSessionService thresholdSessionService;
    private final ProtocolSessionService protocolSessionService;

    /**
     * 라운드 메시지 상태를 확인하고 임계치를 만족하면 메시지를 처리하는 메서드입니다.
     * @param type 프로토콜 타입
     * @param message 메시지 내용
     * @param sid 그룹 id
     */
    public void collectMessageAndCheckCompletion(String type, String message, String sid) {
        log.info("메시지 수신: {}", StringUtils.abbreviate(message, 200));
        // DelegateOutput 파싱
        DelegateOutput output = (DelegateOutput)JsonUtil.parse(message, DelegateOutput.class);
        List<ContinueMessage> continueMessages = output.getContinueMessages();

        // 각 메시지마다 발송 준비 여부 확인 후 발송
        continueMessages.forEach(continueMessage -> {
            if(isMessageReadyToSend(continueMessage)) {
                sendMessage(continueMessage, type, sid, protocolSessionService.getSession(sid));
            }
        });
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
        log.info("{}으로부터 프로토콜 초기화 종료 메시지 받음", memberId);
        thresholdSessionService.addSession(sid);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);
        int threshold = protocolData.getThreshold();

        // 초기화 완료한 클라이언트 수
        int currentCount = thresholdSessionService.getSessionCount(sid);

        // 임계치보다 초기화 완료 클라이언트 수가 많아지면 프로토콜 시작 메시지 전송
        if(currentCount >= threshold) {
            thresholdSessionService.clearSession(sid);
            protocolData.getMemberIds().forEach(recipient -> {
                log.info("{} 프로토콜 시작 메시지 전송: {}", type, recipient);
                InitProtocolEndEvent event = InitProtocolEndEvent.builder()
                        .sid(sid)
                        .type(type)
                        .recipient(recipient)
                        .build();
                tssMessageBroker.publish(event);
            });
        }
    }

    private boolean isMessageReadyToSend(ContinueMessage message) {
        //메시지에서 라운드 추출
        Round round = message.getMessage_type().values().stream().findFirst().map(Round::fromName)
                .orElseThrow(() -> new RuntimeException("타입을 찾을 수 없습니다."));

        // 선행조건이 있으면 보낼 준비 안 됨
        Boolean condition = round.hasPendingPrerequisites();
        log.info("{} message condition: {}", round, !condition);
        return !round.hasPendingPrerequisites();
    }

    /**
     * 단일 메시지 처리 메서드입니다.
     * broadcast 여부에 따라 메시지 수신자를 결정하고 전송합니다.
     * @param message 메시지
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     */
    private void sendMessage(ContinueMessage message, String type, String sid, ProtocolData protocolData) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? protocolData.getMemberIds().stream().filter(mid -> !mid.equals(message.getFrom().toString())).toList() // Is_broadcast이면 보내는 사람을 제외한 모든 참여자
                : List.of(message.getTo().toString()); // Is_broadcast가 false이면 한명

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            log.info("{}에게 메시지 전송 : {}", recipient ,StringUtils.abbreviate(JsonUtil.toString(message), 200));
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
        log.info("{}으로부터 프로토콜 종료 메시지 수신", memberId);

        // 종료 상태 세션에 추가
        thresholdSessionService.addSession(sid);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);

        // 프로토콜 종료 클라이언트 수 조회
        int currentCount = thresholdSessionService.getSessionCount(sid);

        // 임계치보다 종료 상태 세션수가 많아지고 다음 프로토콜이 존재하면 시작 메시지 전송
        // 다음 프로토콜 존재하지 않으면 완전 종료
        // TODO: 프로토콜에 따라서 다음 과정 진행
        if(currentCount >= protocolData.getThreshold()) {
            thresholdSessionService.clearSession(sid);
            type.getNextStep().ifPresentOrElse(
                    nextType -> {
                        log.info("{} 종료, {} 실행", type, nextType);
                        sendInitMessage(protocolData.getMemberIds(), nextType, sid, protocolData.getThreshold(), protocolData.getMessageBytes());
                    },
                    () -> {
                        log.info("모든 참여자 종료");
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
            String[] otherIds = memberIds.stream().filter(id -> !id.equals(recipient)).toList().toArray(String[]::new);

            // 메시지 전송
            InitProtocolEvent event = InitProtocolEvent.builder()
                    .participantType(type)
                    .sid(sid)
                    .otherIds(otherIds)
                    .threshold(threshold)
                    .messageBytes(messageBytes)
                    .recipient(recipient)
                    .build();
            log.info("{}에게 {} 프로토콜 초기화 메시지 전송", recipient, type);
            tssMessageBroker.publish(event);
        });
    }
}
