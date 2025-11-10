package com.zkrypto.zk_mpc_core.application.tss;

import com.zkrypto.constant.ParticipantType;
import com.zkrypto.constant.ProcessGroup;
import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.application.session.thresholdSessionService;
import com.zkrypto.zk_mpc_core.application.session.MessageSessionService;
import com.zkrypto.zk_mpc_core.application.session.ProtocolSessionService;
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
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class TssService {
    private final MessageBroker tssMessageBroker;
    private final MessageSessionService messageSessionService;
    private final thresholdSessionService thresholdSessionService;
    private final ProtocolSessionService protocolSessionService;

    /**
     * 프로세스 그룹에 따라서 프로토콜을 초기화하는 메시지를 전송하는 메서드입니다.
     * @param command command
     */
    public void initProtocol(InitProtocolCommand command) {
        // 프로토콜 데이터 저장
        ProtocolData protocolData = new ProtocolData(command.process(), command.memberIds(), command.threshold(), command.messageBytes(), command.target());
        protocolSessionService.addSession(command.sid(), protocolData);

        command.memberIds().forEach(recipient -> {
            // 실행해야하는 첫번째 프로토콜 조회
            ParticipantType participantType = ParticipantType.getFirstStep(command.process());

            // 실행 메시지 전송
            sendInitMessage(command.memberIds(), recipient, participantType, command.sid(), command.threshold(), command.messageBytes(), command.target());
        });
    }

    /**
     * 프로토콜 참여자들의 초기화 상태를 확인하고 모두 초기화가 완료되면 프로토콜 시작 메시지를 전송하는 메서드입니다.
     * 참여자가 모두 초기화를 완료하면 프로토콜을 시작합니다.
     * @param sid 그룹 id
     * @param memberId 멤버 id
     * @param type 프로토콜 타입
     */
    public void confirmInitiation(String sid, String memberId, ParticipantType type) {
        // 팩토리 세션에 추가
        log.info("{}으로부터 프로토콜 초기화 종료 메시지 받음", memberId);
        Integer currentCount = thresholdSessionService.addSession(sid);

        checkThresholdAndExecute(sid, currentCount, (protocolData -> {
            // TRECOVERHELPER, TRECOVERTARGET 초기화 후에는 helper만 프로토콜 시작하도록 변경
            List<String> participantIds = type.equals(ParticipantType.TRECOVERHELPER) || type.equals(ParticipantType.TRECOVERTARGET) ?
                    protocolData.getParticipantIds().stream().filter(id -> !id.equals(protocolData.getTarget())).toList():
                    protocolData.getParticipantIds();
            protocolSessionService.setParticipants(sid, participantIds);

            // 프로토콜 참여자들에게 메시지 전송
            participantIds.forEach(recipient -> {
                log.info("{} 프로토콜 시작 메시지 전송: {}", type, recipient);
                InitProtocolEndEvent event = InitProtocolEndEvent.builder()
                        .sid(sid)
                        .type(type)
                        .recipient(recipient)
                        .build();
                tssMessageBroker.publish(event);
            });
        }));
    }

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
                sendRoundMessage(continueMessage, type, sid, protocolSessionService.getSession(sid));
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
    public void confirmRoundStatus(String type, String roundName, String sid) {
        // 순서 상관없는 라운드이면 스킵
        if(Round.fromName(roundName).getNextRound().isEmpty()) {
            return;
        }

        // 세션에 추가
        String sessionKey = sid.concat(type.concat(roundName)); // sid, type, roundName 을 조합해서 key 생성
        thresholdSessionService.addSession(sessionKey);

        // 세션, 임계치 조회
        int totalParticipants = protocolSessionService.getSession(sid).getParticipantIds().size();
        int sessionCount = thresholdSessionService.getSessionCount(sessionKey);

        // 라운드 종료 수가 임계치를 넘으면 다음 라운드 진행
        if(sessionCount >= totalParticipants) {
            log.info("round name : {}", roundName);
            Round nextRound = Round.fromName(roundName).getNextRound()
                    .orElseThrow(() -> new RuntimeException("다음 라운드가 존재하지 않습니다."));
            String key = sid.concat(type.concat(nextRound.name()));
            List<ContinueMessage> nextMessage = messageSessionService.getSessionMessage(key);
            messageSessionService.clearSession(key);
            nextMessage.forEach(m -> sendRoundMessage(m, type, sid, protocolSessionService.getSession(sid)));
        }
    }

    /**
     * 프로토콜 종료 상태를 확인하는 메서드입니다.
     * 프로토콜 참여자 모두가 종료 상태이면 다음 프로토콜을 진행합니다.
     * @param sid 그룹 id
     * @param memberId 클라이언트 id
     * @param type 메시지 타입
     */
    public void confirmProtocolCompletion(String sid, String memberId, ParticipantType type) {
        log.info("{}으로부터 프로토콜 종료 메시지 수신", memberId);
        Integer currentCount = thresholdSessionService.addSession(sid);

        checkThresholdAndExecute(sid, currentCount, (protocolData -> {
            advanceToNextStepOrFinalize(sid, type, protocolData);
        }));
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
     * 세션 카운트가 임계치에 도달했는지 확인하고, 도달했다면 후속 작업을 실행하는 메서드입니다.
     * @param sid 세션 ID (그룹 ID)
     * @param onThresholdReached 임계치 도달 시 실행할 작업
     */
    private void checkThresholdAndExecute(String sid, int currentCount, Consumer<ProtocolData> onThresholdReached) {
        // 현재 그룹의 프로토콜 정보 조회
        ProtocolData protocolData = protocolSessionService.getSession(sid);

        // 임계치 (전체 참여자 수) 및 현재 카운트 조회
        int totalParticipants = protocolData.getParticipantIds().size();

        log.info("total participants : {}, current: {}", totalParticipants, currentCount);

        // 임계치 도달 여부 확인
        if (currentCount >= totalParticipants) {
            // 임계치 세션 정리 (중복 실행 방지)
            thresholdSessionService.clearSession(sid);

            // 전달받은 고유 작업 실행
            onThresholdReached.accept(protocolData);
        }
    }

    /**
     * 현재 프로토콜 단계를 완료하고, 다음 단계로 진행하거나 세션을 종료합니다.
     * @param sid 세션 ID
     * @param currentType 현재 완료된 프로토콜 타입
     * @param protocolData 프로토콜 데이터
     */
    private void advanceToNextStepOrFinalize(String sid, ParticipantType currentType, ProtocolData protocolData) {
        // 현재 프로토콜 타입의 다음 단계를 조회
        Optional<ParticipantType> nextStep = currentType.getNextStep(protocolData.getProcessGroup());

        if (nextStep.isPresent()) {
            // 다음 단계 진행
            log.info("{} 종료, {} 실행", currentType, nextStep.get());
            initiateNextProtocolStep(sid, nextStep.get(), protocolData);
        } else {
            // 다음 단계가 없으면 프로세스 종료
            // 프로토콜 세션 정리
            protocolSessionService.clearSession(sid);
            log.info("모든 참여자 종료");
        }
    }

    /**
     * 다음 프로토콜을 실행하는 메서드입니다.
     * @param sid 그룹 id
     * @param nextType 다음 프로토콜 타입
     * @param protocolData 프로토콜 데이터
     */
    private void initiateNextProtocolStep(String sid, ParticipantType nextType, ProtocolData protocolData) {
        protocolData.getParticipantIds().forEach(recipient -> {
            ParticipantType recipientType = nextType.determineType(recipient, protocolData.getTarget());

            sendInitMessage(
                    protocolData.getMemberIds(),
                    recipient,
                    recipientType,
                    sid,
                    protocolData.getThreshold(),
                    protocolData.getMessageBytes(),
                    protocolData.getTarget()
            );
        });
    }

    /**
     * 프로토콜 초기화 메시지를 전송하는 메서드입니다.
     * @param memberIds 프로토콜 참여자 id
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     * @param threshold 임계치
     * @param messageBytes 메시지
     */
    private void sendInitMessage(List<String> memberIds, String recipient, ParticipantType type, String sid, Integer threshold, byte[] messageBytes, String target) {
        // 해당 메시지를 받는 사람을 제외한 otherIds 생성
        String[] otherIds = memberIds.stream().filter(id -> !id.equals(recipient)).toArray(String[]::new);

        // recover이면 target 을 제외한 participantIds 생성 아니면 member 모두 참여자
        String[] participantIds = type.getProcessGroup().equals(ProcessGroup.RECOVER) ?
                memberIds.stream().filter(id -> !id.equals(target)).toArray(String[]::new) :
                memberIds.toArray(String[]::new);

        // 메시지 전송
        InitProtocolEvent event = InitProtocolEvent.builder()
                .participantType(type)
                .sid(sid)
                .otherIds(otherIds)
                .threshold(threshold)
                .messageBytes(messageBytes)
                .participantIds(participantIds)
                .recipient(recipient)
                .target(target)
                .build();
        log.info("{}에게 {} 프로토콜 초기화 메시지 전송", recipient, type);
        tssMessageBroker.publish(event);
    }

    /**
     * 단일 라운드 메시지 처리 메서드입니다.
     * broadcast 여부에 따라 메시지 수신자를 결정하고 전송합니다.
     * @param message 메시지
     * @param type 프로토콜 타입
     * @param sid 그룹 id
     */
    private void sendRoundMessage(ContinueMessage message, String type, String sid, ProtocolData protocolData) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? protocolData.getMemberIds().stream().filter(mid -> !mid.equals(message.getFrom().toString())).toList() // Is_broadcast이면 보내는 사람을 제외한 모든 참여자
                : List.of(message.getTo().toString()); // Is_broadcast가 false이면 한명

        boolean isTargetMessageCondition = type.equals(ParticipantType.TRECOVERHELPER.getTypeName())
                && message.extractRound().equals(Round.R3_ENCRYPTED_SHARE);

        // 타입 결정
        String messageType = isTargetMessageCondition
                ? ParticipantType.TRECOVERTARGET.getTypeName()
                : type;

        // 참여자 결정
        if(isTargetMessageCondition) {
            protocolSessionService.setParticipants(sid, List.of(protocolData.getTarget()));
        }

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            log.info("{}에게 메시지 전송 : {}", recipient ,StringUtils.abbreviate(JsonUtil.toString(message), 200));
            MessageProcessEndEvent event = MessageProcessEndEvent.builder()
                    .recipient(recipient)
                    .message(JsonUtil.toString(message))
                    .type(messageType)
                    .sid(sid)
                    .build();
            tssMessageBroker.publish(event);
        });
    }
}
