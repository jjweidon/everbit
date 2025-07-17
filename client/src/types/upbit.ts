export interface UpbitAccount {
    currency: string; // 화폐를 의미하는 영문 대문자 코드
    balance: string; // 주문가능 금액/수량
    locked: string; // 주문 중 묶여있는 금액/수량
    avg_buy_price: string; // 매수평균가
    avg_buy_price_modified: boolean; // 매수평균가 수정 여부
    unit_currency: string; // 평단가 기준 화폐
}

export interface AccountSummary {
    totalBalance: number; // 총 보유 자산
    availableBalance: number; // 사용 가능한 잔액
    totalProfit: number; // 총 평가 손익
    profitRate: number; // 수익률
    accounts: UpbitAccount[]; // 계좌 목록
}
