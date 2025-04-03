#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
전략 팩토리 모듈
"""

import logging
import os
import sys
from typing import Dict, Optional, Type, Union

# 모듈 경로 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.dirname(current_dir)
sys.path.insert(0, os.path.dirname(src_dir))

from data.upbit_api import UpbitAPI
from strategies.base_strategy import BaseStrategy
from strategies.bollinger_rsi import BollingerRSIStrategy
from strategies.ma_crossover import MACrossoverStrategy

logger = logging.getLogger(__name__)

# 사용 가능한 전략 클래스 맵
STRATEGIES = {
    "ma_crossover": MACrossoverStrategy,
    "bollinger_rsi": BollingerRSIStrategy,
}


def create_strategy(
    strategy_name: str,
    api: UpbitAPI,
    symbol: str,
    interval: str,
    params: Optional[Dict] = None,
) -> BaseStrategy:
    """
    트레이딩 전략 생성 함수

    Parameters
    ----------
    strategy_name : str
        전략 이름 (ma_crossover, bollinger_rsi)
    api : UpbitAPI
        Upbit API 인스턴스
    symbol : str
        거래 심볼 (예: "KRW-BTC")
    interval : str
        캔들 간격
    params : Optional[Dict]
        전략 매개변수

    Returns
    -------
    BaseStrategy
        생성된 전략 인스턴스

    Raises
    ------
    ValueError
        지원되지 않는 전략 이름
    """
    if strategy_name not in STRATEGIES:
        supported = ", ".join(STRATEGIES.keys())
        raise ValueError(f"지원되지 않는 전략: {strategy_name}. 지원되는 전략: {supported}")

    strategy_class = STRATEGIES[strategy_name]
    logger.info(f"'{strategy_name}' 전략 생성")
    return strategy_class(api, symbol, interval, params)