#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
트레이딩 전략 팩토리 모듈
"""

import logging
from typing import Dict, Optional

from src.data.upbit_api import UpbitAPI
from src.strategies.base_strategy import BaseStrategy
from src.strategies.ma_crossover import MACrossoverStrategy

logger = logging.getLogger(__name__)

STRATEGIES = {
    "ma_crossover": MACrossoverStrategy,
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
        전략 이름 (ma_crossover, rsi, macd, bollinger)
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