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
     * 라운드를 진행하는 메서드입니다.
     * 메시지마다 발송 조건을 확인합니다.
     * 발송 조건이 만족되는 메시지는 클라이언트에 전달하고
     * 만족되지 않은 메시지는 세션에 저장합니다.
     * @param type 프로토콜 타입
     * @param message 메시지 내용
     * @param sid 그룹 id
     */
    public void proceedRound(String type, String message, String sid) {
        log.info("메시지 수신: {}", StringUtils.abbreviate(message, 200));
        // DelegateOutput 파싱
        DelegateOutput output = (DelegateOutput)JsonUtil.parse(message, DelegateOutput.class);
        List<ContinueMessage> continueMessages = output.getContinueMessages();

        // 각 메시지마다 발송 준비 여부 확인 후 발송
        continueMessages.forEach(continueMessage -> {
            if(isMessageReadyToSend(type, continueMessage)) {
                sendMessage(continueMessage, type, sid, protocolSessionService.getSession(sid));
            }
            else {
                keepMessage(continueMessage, type, sid);
            }
        });
    }

    /**
     * 라운드 상태를 확인하는 메서드입니다.
     * 선행조건인 메시지를 받아서 메시지를 모읍니다.
     * 메시지의 임계치를 넘어가면 다음 라운드의 메시지를 클라이언트에게 전달합니다.
     * @param type 메시지 타입
     * @param roundName 라운드 이름
     * @param sid 그룹 id
     */
    public void checkRoundStatus(String type, String roundName, String sid) {
        // 순서 상관없는 라운드이면 스킵
        if(Round.fromName(roundName).getNextRound().isEmpty()) {
            return;
        }

        // 세션에 추가
        String sessionKey = sid.concat(type.concat(roundName)); // sid, type, roundName 을 조합해서 key 생성
        thresholdSessionService.addSession(sessionKey);

        // 세션, 임계치 조회
        int totalParticipants = protocolSessionService.getSession(sid).getMemberIds().size();
        int sessionCount = thresholdSessionService.getSessionCount(sessionKey);

        // 라운드 종료 수가 임계치를 넘으면 다음 라운드 진행
        if(sessionCount >= totalParticipants) {
            log.info("round name : {}", roundName);
            Round nextRound = Round.fromName(roundName).getNextRound()
                    .orElseThrow(() -> new RuntimeException("다음 라운드가 존재하지 않습니다."));
            String key = sid.concat(type.concat(nextRound.name()));
            List<ContinueMessage> nextMessage = messageSessionService.getSessionMessage(key);
            messageSessionService.clearSession(key);
            nextMessage.forEach(m -> sendMessage(m, type, sid, protocolSessionService.getSession(sid)));
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
        log.info("{}으로부터 프로토콜 초기화 종료 메시지 받음", memberId);
        thresholdSessionService.addSession(sid);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);
        int totalParticipants = protocolSessionService.getSession(sid).getMemberIds().size();

        // 초기화 완료한 클라이언트 수
        int currentCount = thresholdSessionService.getSessionCount(sid);

        // 임계치보다 초기화 완료 클라이언트 수가 많아지면 프로토콜 시작 메시지 전송
        if(currentCount >= totalParticipants) {
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

    /**
     * 메시지 전송 여부 확인 메서드입니다.
     * TShare의 R2Decommit는 R2_PRIVATE_SHARE를 모두 받은 후 실행해야합니다.
     * TPresign의 ROUND_ONE, ROUND_TWO는 ROUND_ONE_BROADCAST, ROUND_TWO_BROADCAST를 모두 받은 후 실행해야합니다.
     * R2Decommit, ROUND_ONE, ROUND_TWO는 메시지를 클라이언트에게 보내지 않고 세션에 저장해둔 후, 선행조건이 완료되면 전송합니다.
     * @param type 메시지 타입
     * @param message 메시지
     * @return 메시지 전송 여부
     */
    private boolean isMessageReadyToSend(String type, ContinueMessage message) {
        //메시지에서 라운드 추출
        Round round = message.extractRound();

        // 선행조건 확인
        boolean PendingPrerequisites = round.hasPendingPrerequisites();

        // 선행조건이 있고, TShare, TPresign 프로토콜일 때는 메시지 대기
        return !(PendingPrerequisites && (ParticipantType.of(type).equals(ParticipantType.TSHARE) || ParticipantType.of(type).equals(ParticipantType.TPRESIGN)));
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
     * 메시지를 저장하는 메서드입니다.
     * @param message 메시지
     * @param type 메시지 타입
     * @param sid 그룹 id
     */
    private void keepMessage(ContinueMessage message, String type, String sid) {
        //메시지에서 라운드 추출
        Round round = message.extractRound();

        // 그룹id, 메세지 타입, 라운드를 합쳐서 세션키 생성
        String key = sid.concat(type.concat(round.name()));

        // 저장
        messageSessionService.addSession(key, message);
    }

    /**
     * 프로토콜 종료 상태를 확인하는 메서드입니다.
     * 프로토콜 참여자 모두가 종료 상태이면 다음 프로토콜을 진행합니다.
     * @param sid 그룹 id
     * @param memberId 클라이언트 id
     * @param type 메시지 타입
     */
    public void checkProtocolCompleteStatus(String sid, String memberId, ParticipantType type) {
        log.info("{}으로부터 프로토콜 종료 메시지 수신", memberId);

        // 종료 상태 세션에 추가
        thresholdSessionService.addSession(sid);

        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);
        int totalParticipants = protocolSessionService.getSession(sid).getMemberIds().size();

        // 프로토콜 종료 클라이언트 수 조회
        int currentCount = thresholdSessionService.getSessionCount(sid);

        // 임계치보다 종료 상태 세션수가 많아지고 다음 프로토콜이 존재하면 시작 메시지 전송
        // 다음 프로토콜 존재하지 않으면 완전 종료
        if(currentCount >= totalParticipants) {
            thresholdSessionService.clearSession(sid);
            type.getNextStep().ifPresentOrElse(
                    nextType -> {
                        log.info("{} 종료, {} 실행", type, nextType);
                        sendInitMessage(protocolData.getMemberIds(), nextType, sid, protocolData.getThreshold(), protocolData.getMessageBytes());
                    },
                    () -> {
                        // TODO: 프로토콜에 따라서 다음 과정 진행 (tshare: 퍼블릭키 저장, tsign: 블록체인 업로드)
                        protocolSessionService.clearSession(sid);
                        log.info("모든 참여자 종료");
                    });
        }
    }

    /**
     * 프로세스 그룹에 따라서 프로토콜을 시작하는 메서드입니다.
     * @param command command
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
            String[] participantIds = memberIds.toArray(String[]::new);

            // 메시지 전송
            InitProtocolEvent event = InitProtocolEvent.builder()
                    .participantType(type)
                    .sid(sid)
                    .otherIds(otherIds)
                    .threshold(threshold)
                    .messageBytes(messageBytes)
                    .participantIds(participantIds)
                    .recipient(recipient)
                    .build();
            log.info("{}에게 {} 프로토콜 초기화 메시지 전송", recipient, type);
            tssMessageBroker.publish(event);
        });
    }
}
