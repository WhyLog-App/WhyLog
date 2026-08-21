/** WebRTC mesh 연결 설정 */
export const RTC_CONFIG: RTCConfiguration = {
  // 공개 STUN만 사용한다. 대칭 NAT 환경에서 붙지 않으면 TURN 도입을 회의 안건으로 올린다.
  iceServers: [{ urls: ["stun:stun.l.google.com:19302"] }],
};

/** 로컬 미디어 확보 실패 시 재시도 횟수. MeetingConnectionOverlay의 maxRetries와 맞춘다. */
export const MAX_MEDIA_RETRIES = 3;
export const MEDIA_RETRY_DELAY_MS = 2000;

/** 발화 감지 */
export const SPEAKING_SAMPLE_INTERVAL_MS = 100;
/** 0~1로 정규화한 RMS 임계값. 낮추면 숨소리에도 반응한다. */
export const SPEAKING_RMS_THRESHOLD = 0.035;
/** 음절 사이 공백으로 표시가 깜빡이지 않도록 유지하는 시간 */
export const SPEAKING_HOLD_MS = 400;
