/**
 * UI에서 툴팁/도움말로 사용하는 용어 설명.
 * 퀀트·트레이딩을 처음 접하는 사용자용 한글 설명.
 * 문서: docs/design/glossary.md
 */
export const TERM_TOOLTIPS = {
  /** 자산·손익 섹션 전체 */
  EQUITY_PNL:
    "자산(Equity): 예수금+보유 코인 평가액 등 총 자산가치. PnL(손익): 실제 발생한 수익/손실(Realized)과 미실현 손익(Unrealized)을 포함합니다.",

  /** 개별 지표 */
  EQUITY: "총 자산가치(예수금 + 보유 코인 시가총액).",

  REALIZED:
    "이미 매매가 끝나서 확정된 손익. 매도한 코인의 (매도금액 - 매수가) 등으로 계산됩니다.",

  UNREALIZED:
    "아직 매도하지 않은 보유 코인의 평가손익. 현재 시세 기준으로 계산한 잠정 손익입니다.",

  /** 실행·리스크 */
  EXECUTION_RISK:
    "실행: 자동매매가 실제 주문을 넣는지 여부. 리스크: 오류·제한(429/418)·미확정(UNKNOWN) 건수 등 운영상 주의할 지표입니다.",

  UNKNOWN_COUNT:
    "주문 요청 후 응답이 불명확해 상태를 확정하지 못한 시도 건수. 자동 재주문은 하지 않으며, 확정·정리는 수동(재조정)으로 진행합니다.",

  KILL_SWITCH:
    "자동매매 전원. OFF면 새 주문 시도가 나가지 않습니다. 이미 접수된 주문은 별도로 취소할 수 있습니다.",

  LAST_ERROR: "가장 최근 발생한 오류 시각. 주문 실패, API 오류 등.",

  /** 주문/마켓 */
  INTENT_TYPE:
    "주문 의도 유형: 진입(ENTRY), 손절(EXIT_STOPLOSS), 목표가(EXIT_TP), 트레일링(EXIT_TRAIL), 시간(EXIT_TIME) 등.",

  ATTEMPT_STATUS:
    "주문 시도 상태: 준비(PREPARED), 전송(SENT), 접수(ACKED), 거절(REJECTED), 제한(THROTTLED), 미확정(UNKNOWN), 중단(SUSPENDED).",

  POSITION_STATUS:
    "포지션 상태: FLAT(미보유), OPEN(보유 중), SUSPENDED(해당 마켓 자동매매 일시 중단).",

  COOLDOWN_UNTIL:
    "다음 신호까지 대기하는 시간이 끝나는 시각. 전략에서 정한 쿨다운 기간 동안 동일 마켓 재진입을 막습니다.",
} as const;

export type TermTooltipKey = keyof typeof TERM_TOOLTIPS;
