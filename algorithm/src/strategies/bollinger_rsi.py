#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
볼린저 밴드 + RSI 조합 전략 모듈
"""

import logging
import os
import sys
from typing import Dict, Optional

import pandas as pd
import talib as ta

# 모듈 경로 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.dirname(current_dir)
sys.path.insert(0, os.path.dirname(src_dir))

from strategies.base_strategy import BaseStrategy

logger = logging.getLogger(__name__)


class BollingerRSIStrategy(BaseStrategy):
    """
    볼린저 밴드 + RSI 조합 전략 클래스
    
    매수 시점: 
    - RSI가 30 이하로 과매도 상태에 진입하고
    - 가격이 볼린저 밴드 하단에 근접하거나 돌파했을 때
    - MACD 히스토그램이 상승 반전 신호를 보일 때
    
    매도 시점:
    - RSI가 70 이상으로 과매수 상태에 진입하고
    - 가격이 볼린저 밴드 상단에 근접하거나 돌파했을 때
    - 또는 볼린저 밴드 폭이 감소하기 시작할 때
    
    손절 및 이익실현:
    - 손절: 매수가 대비 5% 하락 시 손절
    - 이익실현: 
      - 첫 번째 목표: 매수가 대비 10% 상승 시 포지션의 50% 매도
      - 두 번째 목표: 매수가 대비 20% 상승 시 나머지 50% 매도
      - 또는 RSI가 70을 초과하면 포지션의 50% 매도
    """

    def __init__(self, api, symbol, interval, params=None):
        """
        초기화

        Parameters
        ----------
        api : UpbitAPI
            Upbit API 인스턴스
        symbol : str
            거래 심볼 (예: "KRW-BTC")
        interval : str
            캔들 간격
        params : Optional[Dict]
            전략 매개변수
        """
        default_params = {
            "bb_period": 20,        # 볼린저 밴드 기간
            "bb_std": 2.0,          # 볼린저 밴드 표준편차
            "rsi_period": 14,       # RSI 기간
            "macd_fast": 12,        # MACD 단기
            "macd_slow": 26,        # MACD 장기
            "macd_signal": 9,       # MACD 시그널
            "rsi_oversold": 30,     # RSI 과매도 기준
            "rsi_overbought": 70,   # RSI 과매수 기준
            "stop_loss": 0.05,      # 손절 기준 (5%)
            "take_profit1": 0.10,   # 첫 번째 이익실현 기준 (10%)
            "take_profit2": 0.20,   # 두 번째 이익실현 기준 (20%)
            "partial_ratio": 0.5    # 부분 매도 비율 (50%)
        }
        
        if params:
            default_params.update(params)
            
        super().__init__(api, symbol, interval, default_params)
        self.buy_price = None       # 매수 가격
        self.position_size = 0      # 포지션 크기
        self.partial_take_profit = False  # 첫번째 이익실현 완료 여부

    def get_min_data_length(self) -> int:
        """
        전략에 필요한 최소 데이터 길이
        
        Returns
        -------
        int
            필요한 최소 캔들 수
        """
        return max(
            self.params["bb_period"],
            self.params["rsi_period"],
            self.params["macd_slow"] + self.params["macd_signal"]
        ) + 10  # 여유분

    def calculate_indicators(self):
        """
        전략에 필요한 지표 계산
        """
        if len(self.data) < self.get_min_data_length():
            return
            
        # 볼린저 밴드 계산
        self.data["bb_middle"] = ta.SMA(self.data["close"], timeperiod=self.params["bb_period"])
        stddev = ta.STDDEV(self.data["close"], timeperiod=self.params["bb_period"])
        self.data["bb_upper"] = self.data["bb_middle"] + self.params["bb_std"] * stddev
        self.data["bb_lower"] = self.data["bb_middle"] - self.params["bb_std"] * stddev
        
        # 볼린저 밴드 폭
        self.data["bb_width"] = (self.data["bb_upper"] - self.data["bb_lower"]) / self.data["bb_middle"]
        self.data["bb_width_prev"] = self.data["bb_width"].shift(1)
        
        # RSI 계산
        self.data["rsi"] = ta.RSI(self.data["close"], timeperiod=self.params["rsi_period"])
        
        # MACD 계산
        macd, signal, hist = ta.MACD(
            self.data["close"], 
            fastperiod=self.params["macd_fast"],
            slowperiod=self.params["macd_slow"], 
            signalperiod=self.params["macd_signal"]
        )
        self.data["macd"] = macd
        self.data["macd_signal"] = signal
        self.data["macd_hist"] = hist
        self.data["macd_hist_prev"] = hist.shift(1)
        
        # 거래량 분석
        self.data["volume_sma"] = ta.SMA(self.data["volume"], timeperiod=20)
        self.data["volume_ratio"] = self.data["volume"] / self.data["volume_sma"]

    def calculate_signal(self) -> Optional[str]:
        """
        현재 시그널 계산
        
        Returns
        -------
        Optional[str]
            "buy", "sell", "partial_sell" 또는 None
        """
        if len(self.data) < self.get_min_data_length():
            return None
            
        # NaN 값 제거
        last_row = self.data.iloc[-1].copy()
        prev_row = self.data.iloc[-2].copy()
        
        if pd.isna(last_row["bb_middle"]) or pd.isna(last_row["rsi"]) or pd.isna(last_row["macd_hist"]):
            logger.warning("지표 계산에 필요한 데이터가 충분하지 않습니다.")
            return None
        
        current_price = last_row["close"]
        
        # 손절 로직 (포지션이 있고, 매수가가 설정되어 있을 때)
        if self.position == "long" and self.buy_price is not None:
            loss_ratio = (self.buy_price - current_price) / self.buy_price
            if loss_ratio >= self.params["stop_loss"]:
                logger.info(f"손절 조건 만족: 매수가 {self.buy_price}, 현재가 {current_price}, 손실률 {loss_ratio:.2%}")
                return "sell"
        
        # 이익실현 로직 (포지션이 있고, 매수가가 설정되어 있을 때)
        if self.position == "long" and self.buy_price is not None:
            profit_ratio = (current_price - self.buy_price) / self.buy_price
            
            # 두 번째 이익실현 목표 (첫 번째 이익실현 완료 후)
            if self.partial_take_profit and profit_ratio >= self.params["take_profit2"]:
                logger.info(f"두 번째 이익실현 조건 만족: 매수가 {self.buy_price}, 현재가 {current_price}, 수익률 {profit_ratio:.2%}")
                return "sell"
            
            # 첫 번째 이익실현 목표
            if not self.partial_take_profit and profit_ratio >= self.params["take_profit1"]:
                logger.info(f"첫 번째 이익실현 조건 만족: 매수가 {self.buy_price}, 현재가 {current_price}, 수익률 {profit_ratio:.2%}")
                return "partial_sell"
            
        # 매수 시그널: RSI 과매도 + 볼린저 하단 돌파 + MACD 히스토그램 상승 반전
        if (last_row["rsi"] <= self.params["rsi_oversold"] and 
            current_price <= last_row["bb_lower"] and
            last_row["macd_hist"] > last_row["macd_hist_prev"] and
            last_row["volume_ratio"] > 1.0):  # 거래량 증가 확인
            
            logger.info(
                f"매수 시그널 발생! RSI: {last_row['rsi']:.2f}, "
                f"가격: {current_price}, BB 하단: {last_row['bb_lower']:.2f}, "
                f"MACD 히스토그램: {last_row['macd_hist']:.6f}, 이전: {last_row['macd_hist_prev']:.6f}"
            )
            return "buy"
            
        # 매도 시그널: RSI 과매수 + 볼린저 상단 돌파 또는 밴드 폭 감소
        if self.position == "long" and (
            (last_row["rsi"] >= self.params["rsi_overbought"] and current_price >= last_row["bb_upper"]) or
            (last_row["bb_width"] < last_row["bb_width_prev"] and last_row["rsi"] >= self.params["rsi_overbought"])
        ):
            logger.info(
                f"매도 시그널 발생! RSI: {last_row['rsi']:.2f}, "
                f"가격: {current_price}, BB 상단: {last_row['bb_upper']:.2f}, "
                f"BB 폭: {last_row['bb_width']:.4f}, 이전: {last_row['bb_width_prev']:.4f}"
            )
            return "sell"
            
        return None

    def execute(self):
        """
        트레이딩 시그널 실행 (오버라이드)
        """
        if not self.last_signal:
            logger.info("실행할 시그널이 없습니다.")
            return

        try:
            # 시그널에 따라 주문 실행
            if self.last_signal == "buy" and self.position != "long":
                self._execute_buy()
            elif self.last_signal == "sell" and self.position == "long":
                self._execute_sell(1.0)  # 전체 매도
            elif self.last_signal == "partial_sell" and self.position == "long":
                self._execute_sell(self.params["partial_ratio"])  # 부분 매도
        except Exception as e:
            logger.error(f"주문 실행 중 오류 발생: {e}")

    def _execute_buy(self):
        """
        매수 주문 실행 (오버라이드)
        """
        try:
            # 계좌 잔고 확인
            accounts = self.api.get_account()
            krw_balance = 0
            for account in accounts:
                if account["currency"] == "KRW":
                    krw_balance = float(account["balance"])
                    break

            if krw_balance <= 0:
                logger.warning("매수 가능한 KRW 잔고가 없습니다.")
                return

            # 현재가 조회
            ticker = self.api.get_ticker(self.symbol)[0]
            current_price = float(ticker["trade_price"])
            
            # 매수 금액 계산 (전체 잔고의 30% 사용)
            amount = krw_balance * 0.3
            volume = amount / current_price
            
            # 주문 실행
            logger.info(f"매수 주문 실행: {self.symbol}, 가격: {current_price}, 수량: {volume:.8f}")
            result = self.api.order(
                market=self.symbol,
                side="bid",
                price=amount,
                ord_type="price",  # 시장가 매수
            )
            logger.info(f"매수 주문 결과: {result}")
            
            # 매수 상태 업데이트
            self.position = "long"
            self.buy_price = current_price
            self.position_size = volume
            self.partial_take_profit = False
            
        except Exception as e:
            logger.error(f"매수 주문 중 오류 발생: {e}")

    def _execute_sell(self, ratio=1.0):
        """
        매도 주문 실행 (오버라이드)
        
        Parameters
        ----------
        ratio : float
            매도할 보유량 비율 (0.0 ~ 1.0)
        """
        try:
            # 코인 잔고 확인
            accounts = self.api.get_account()
            coin_balance = 0
            for account in accounts:
                if account["currency"] == self.symbol.split("-")[1]:
                    coin_balance = float(account["balance"])
                    break

            if coin_balance <= 0:
                logger.warning(f"매도 가능한 {self.symbol} 잔고가 없습니다.")
                return
            
            # 매도할 수량 계산
            sell_amount = coin_balance * ratio
            
            # 주문 실행
            logger.info(f"매도 주문 실행: {self.symbol}, 수량: {sell_amount:.8f}, 비율: {ratio:.2%}")
            result = self.api.order(
                market=self.symbol,
                side="ask",
                volume=sell_amount,
                ord_type="market",  # 시장가 매도
            )
            logger.info(f"매도 주문 결과: {result}")
            
            # 매도 상태 업데이트
            if ratio >= 1.0:  # 전체 매도
                self.position = None
                self.buy_price = None
                self.position_size = 0
                self.partial_take_profit = False
            else:  # 부분 매도
                self.position_size = self.position_size * (1 - ratio)
                self.partial_take_profit = True
                
        except Exception as e:
            logger.error(f"매도 주문 중 오류 발생: {e}") 