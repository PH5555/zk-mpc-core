package com.zkrypto.cryptolib;

public class TssBridge {
    /**
     * 지정된 타입의 프로토콜 참여자(Participant)를 생성하고 초기화합니다.
     *
     * @param participantType 참여자 타입 (e.g., "AuxInfo", "TShare")
     * @param sid 세션 ID
     * @param id 자신의 참여자 ID
     * @param otherParticipantIds 자신을 제외한 다른 모든 참여자들의 ID 배열
     * @param input 프로토콜에 필요한 초기 입력값 (JSON 문자열)
     */
    public static native void participantFactory(
            String participantType,
            String sid,
            String id,
            String[] otherParticipantIds,
            String input
    );

    /**
     * 프로토콜 시작을 위한 초기 'Ready' 메시지를 생성합니다.
     *
     * @param participantType 참여자 타입
     * @param sid 세션 ID
     * @param myId 자신의 참여자 ID
     * @return 생성된 'Ready' 메시지 (JSON 문자열)
     */
    public static native String readyMessageFactory(String participantType, String sid, String myId);

    /**
     * 다른 참여자로부터 받은 메시지를 처리하고 다음 단계의 메시지를 생성합니다.
     *
     * @param participantType 현재 진행 중인 프로토콜의 참여자 타입
     * @param message 처리할 메시지 (JSON 문자열)
     * @return 처리 결과로 생성된 다음 메시지 (JSON 문자열)
     */
    public static native String delegateProcessMessage(String participantType, String message);

    /**
     * TShare 프로토콜에 필요한 입력값을 생성합니다.
     *
     * @param auxinfoOutput AuxInfo 프로토콜의 결과물 (JSON 문자열)
     * @param threshold 서명에 필요한 최소 참여자 수 (임계값)
     * @return TShare 입력값 (JSON 문자열)
     */
    public static native String generateTshareInput(String auxinfoOutput, int threshold);

    /**
     * TRefresh 프로토콜에 필요한 입력값을 생성합니다.
     *
     * @param tshareOutput TShare 프로토콜의 결과물 (JSON 문자열)
     * @param auxinfoOutput AuxInfo 프로토콜의 결과물 (JSON 문자열)
     * @param threshold 임계값
     * @return TRefresh 입력값 (JSON 문자열)
     */
    public static native String generateTrefreshInput(String tshareOutput, String auxinfoOutput, int threshold);

    /**
     * TPreSign 프로토콜에 필요한 입력값을 생성합니다.
     *
     * @param signerParticipantIds 서명에 참여할 참여자들의 ID 배열
     * @param auxinfoOutput AuxInfo 프로토콜의 결과물 (JSON 문자열)
     * @param tshareOutput TShare 프로토콜의 결과물 (JSON 문자열)
     * @return TPreSign 입력값 (JSON 문자열)
     */
    public static native String generateTpresignInput(String[] signerParticipantIds, String auxinfoOutput, String tshareOutput);

    /**
     * Sign 프로토콜에 필요한 입력값을 생성합니다.
     *
     * @param myId 자신의 참여자 ID
     * @param signerParticipantIds 서명에 참여할 참여자들의 ID 배열 (자신 포함)
     * @param tpresignOutput TPreSign 프로토콜의 결과물 (JSON 문자열)
     * @param tshareOutput TShare 프로토콜의 결과물 (JSON 문자열)
     * @param messageBytes 서명할 메시지 원본 바이트 배열
     * @param threshold 임계값
     * @return Sign 입력값 (JSON 문자열)
     */
    public static native String generateSignInput(
            String myId,
            String[] signerParticipantIds,
            String tpresignOutput,
            String tshareOutput,
            byte[] messageBytes,
            int threshold
    );

    /**
     * 여러 참여자로부터 받은 서명 조각(share)들을 모아 최종 ECDSA 서명을 완성합니다.
     *
     * @param messages 각 참여자가 생성한 Sign 프로토콜의 결과 메시지 배열
     * @return 최종 서명 (JSON 문자열)
     */
    public static native String computeOutput(String[] messages);
}