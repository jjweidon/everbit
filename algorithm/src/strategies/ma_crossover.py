#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
이동평균선 크로스오버 전략 모듈
"""

import logging
from typing import Dict, Optional

import pandas as pd
import talib as ta

from src.strategies.base_strategy import BaseStrategy

logger = logging.getLogger(__name__)


class MACrossoverStrategy(BaseStrategy):
    """
    이동평균선 크로스오버 전략 클래스
    단기 이동평균선이 장기 이동평균선을 상향 돌파하면 매수
    단기 이동평균선이 장기 이동평균선을 하향 돌파하면 매도
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
            전략 매개변수 (short_period, long_period)
        """
        default_params = {
            "short_period": 10,  # 단기 이동평균선 기간
            "long_period": 30,   # 장기 이동평균선 기간
        }
        
        if params:
            default_params.update(params)
            
        super().__init__(api, symbol, interval, default_params)

    def get_min_data_length(self) -> int:
        """
        전략에 필요한 최소 데이터 길이
        
        Returns
        -------
        int
            필요한 최소 캔들 수
        """
        return self.params["long_period"] + 5  # 장기 이동평균선 기간 + 여유분

    def calculate_indicators(self):
        """
        이동평균선 지표 계산
        """
        if len(self.data) < self.get_min_data_length():
            return
            
        # 단기 이동평균선 계산
        self.data["ma_short"] = ta.SMA(self.data["close"], timeperiod=self.params["short_period"])
        
        # 장기 이동평균선 계산
        self.data["ma_long"] = ta.SMA(self.data["close"], timeperiod=self.params["long_period"])
        
        # 전일 이동평균선 값 계산 (크로스오버 감지용)
        self.data["ma_short_prev"] = self.data["ma_short"].shift(1)
        self.data["ma_long_prev"] = self.data["ma_long"].shift(1)

    def calculate_signal(self) -> Optional[str]:
        """
        현재 시그널 계산
        
        Returns
        -------
        Optional[str]
            "buy", "sell" 또는 None
        """
        if len(self.data) < self.get_min_data_length():
            return None
            
        # NaN 값 제거
        last_row = self.data.iloc[-1].copy()
        prev_row = self.data.iloc[-2].copy()
        
        if pd.isna(last_row["ma_short"]) or pd.isna(last_row["ma_long"]) or \
           pd.isna(prev_row["ma_short"]) or pd.isna(prev_row["ma_long"]):
            logger.warning("이동평균선 계산에 필요한 데이터가 충분하지 않습니다.")
            return None
            
        # 골든 크로스 (매수 시그널): 단기가 장기를 상향 돌파
        if prev_row["ma_short"] <= prev_row["ma_long"] and last_row["ma_short"] > last_row["ma_long"]:
            logger.info(
                f"골든 크로스 발생! 단기 MA: {last_row['ma_short']:.2f}, 장기 MA: {last_row['ma_long']:.2f}"
            )
            return "buy"
            
        # 데드 크로스 (매도 시그널): 단기가 장기를 하향 돌파
        elif prev_row["ma_short"] >= prev_row["ma_long"] and last_row["ma_short"] < last_row["ma_long"]:
            logger.info(
                f"데드 크로스 발생! 단기 MA: {last_row['ma_short']:.2f}, 장기 MA: {last_row['ma_long']:.2f}"
            )
            return "sell"
            
        return None