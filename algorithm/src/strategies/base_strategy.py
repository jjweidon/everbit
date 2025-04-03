#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
기본 트레이딩 전략 모듈
"""

import logging
import os
import sys
from abc import ABC, abstractmethod
from typing import Dict, List, Optional

import pandas as pd

# 모듈 경로 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.dirname(current_dir)
sys.path.insert(0, os.path.dirname(src_dir))

from data.upbit_api import UpbitAPI

logger = logging.getLogger(__name__)


class BaseStrategy(ABC):
    """
    모든 트레이딩 전략의 기본 클래스
    """

    def __init__(
        self, api: UpbitAPI, symbol: str, interval: str, params: Optional[Dict] = None
    ):
        """
        초기화

        Parameters
        ----------
        api : UpbitAPI
            Upbit API 인스턴스
        symbol : str
            거래 심볼 (예: "KRW-BTC")
        interval : str
            캔들 간격 (minute1, minute3, minute5, minute10, minute15, minute30, minute60, minute240, day, week, month)
        params : Optional[Dict]
            전략 매개변수
        """
        self.api = api
        self.symbol = symbol
        self.interval = interval
        self.params = params or {}
        self.data = pd.DataFrame()
        self.last_signal = None
        self.position = None
        self._check_position()

    def _check_position(self):
        """
        현재 포지션 확인
        """
        try:
            accounts = self.api.get_account()
            for account in accounts:
                if account["currency"] == self.symbol.split("-")[1]:
                    self.position = "long" if float(account["balance"]) > 0 else None
                    return
            self.position = None
        except Exception as e:
            logger.error(f"포지션 확인 중 오류 발생: {e}")
            self.position = None

    def update(self):
        """
        데이터 업데이트 및 시그널 계산
        """
        try:
            # 최신 캔들 데이터 가져오기
            self.data = self.api.get_candles(self.symbol, self.interval)
            
            # 데이터가 충분한지 확인
            if len(self.data) < self.get_min_data_length():
                logger.warning(f"데이터가 충분하지 않습니다: {len(self.data)} < {self.get_min_data_length()}")
                return

            # 지표 계산
            self.calculate_indicators()
            
            # 시그널 계산
            self.last_signal = self.calculate_signal()
            logger.info(f"계산된 시그널: {self.last_signal}")
            
        except Exception as e:
            logger.error(f"데이터 업데이트 중 오류 발생: {e}")

    def execute(self):
        """
        트레이딩 시그널 실행
        """
        if not self.last_signal:
            logger.info("실행할 시그널이 없습니다.")
            return

        try:
            # 시그널에 따라 주문 실행
            if self.last_signal == "buy" and self.position != "long":
                self._execute_buy()
            elif self.last_signal == "sell" and self.position == "long":
                self._execute_sell()
        except Exception as e:
            logger.error(f"주문 실행 중 오류 발생: {e}")

    def _execute_buy(self):
        """
        매수 주문 실행
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
            
            # 매수 금액 계산 (전체 잔고의 90% 사용)
            amount = krw_balance * 0.9
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
            self.position = "long"
        except Exception as e:
            logger.error(f"매수 주문 중 오류 발생: {e}")

    def _execute_sell(self):
        """
        매도 주문 실행
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
            
            # 주문 실행
            logger.info(f"매도 주문 실행: {self.symbol}, 수량: {coin_balance:.8f}")
            result = self.api.order(
                market=self.symbol,
                side="ask",
                volume=coin_balance,
                ord_type="market",  # 시장가 매도
            )
            logger.info(f"매도 주문 결과: {result}")
            self.position = None
        except Exception as e:
            logger.error(f"매도 주문 중 오류 발생: {e}")

    @abstractmethod
    def calculate_indicators(self):
        """
        전략에 필요한 지표 계산
        """
        pass

    @abstractmethod
    def calculate_signal(self) -> Optional[str]:
        """
        현재 시그널 계산
        
        Returns
        -------
        Optional[str]
            "buy", "sell" 또는 None
        """
        pass

    @abstractmethod
    def get_min_data_length(self) -> int:
        """
        전략에 필요한 최소 데이터 길이
        
        Returns
        -------
        int
            필요한 최소 캔들 수
        """
        pass 