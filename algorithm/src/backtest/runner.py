#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
백테스팅 실행기 모듈
"""

import logging
from datetime import datetime
from typing import Dict, List, Optional

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from dateutil.parser import parse

from src.data.upbit_api import UpbitAPI
from src.strategies.factory import create_strategy, STRATEGIES

logger = logging.getLogger(__name__)


class BacktestRunner:
    """
    백테스팅 실행기 클래스
    """

    def __init__(
        self,
        strategy_name: str,
        symbol: str,
        start_date: str,
        end_date: str,
        interval: str = "day",
        initial_capital: float = 1000000.0,
    ):
        """
        초기화

        Parameters
        ----------
        strategy_name : str
            전략 이름
        symbol : str
            거래 심볼 (예: "KRW-BTC")
        start_date : str
            백테스팅 시작일 (YYYY-MM-DD)
        end_date : str
            백테스팅 종료일 (YYYY-MM-DD)
        interval : str
            캔들 간격
        initial_capital : float
            초기 자본
        """
        self.strategy_name = strategy_name
        self.symbol = symbol
        self.start_date = parse(start_date)
        self.end_date = parse(end_date)
        self.interval = interval
        self.initial_capital = initial_capital
        self.api = UpbitAPI("dummy", "dummy")  # 백테스팅 API
        self.data = pd.DataFrame()
        self.results = pd.DataFrame()

    def _load_data(self) -> pd.DataFrame:
        """
        백테스팅 데이터 로드

        Returns
        -------
        pd.DataFrame
            캔들 데이터
        """
        # 실제로는 API에서 데이터를 가져오지만, 백테스팅에서는 과거 데이터를 사용
        # 여기서는 파일에서 로드하거나, 데이터베이스에서 가져오는 등의 방법 사용
        try:
            # 예시: CSV 파일 로드
            # data = pd.read_csv(f"data/{self.symbol}_{self.interval}.csv", index_col="datetime", parse_dates=True)
            
            # 백테스팅을 위한 데이터 직접 요청 (실제 API 사용)
            # 실제 구현 시 더 많은 데이터를 가져오고 필터링해야 함
            data = self.api.get_candles(self.symbol, self.interval, count=200)
            
            # 시작일과 종료일로 필터링
            mask = (data.index >= self.start_date) & (data.index <= self.end_date)
            data = data.loc[mask]
            
            return data
        except Exception as e:
            logger.error(f"데이터 로드 중 오류 발생: {e}")
            return pd.DataFrame()

    def run(self) -> Dict:
        """
        백테스팅 실행

        Returns
        -------
        Dict
            백테스팅 결과
        """
        logger.info(f"{self.strategy_name} 전략 백테스팅 시작")
        logger.info(f"기간: {self.start_date.strftime('%Y-%m-%d')} ~ {self.end_date.strftime('%Y-%m-%d')}")
        
        # 데이터 로드
        self.data = self._load_data()
        if self.data.empty:
            logger.error("백테스팅 데이터가 없습니다.")
            return {"error": "데이터 로드 실패"}
            
        # 전략 생성
        strategy_class = STRATEGIES.get(self.strategy_name)
        if not strategy_class:
            logger.error(f"지원되지 않는 전략: {self.strategy_name}")
            return {"error": f"지원되지 않는 전략: {self.strategy_name}"}
            
        # 백테스팅 준비
        capital = self.initial_capital
        position = 0.0
        trades = []
        results = {
            "datetime": [],
            "price": [],
            "capital": [],
            "position": [],
            "signal": [],
            "portfolio_value": [],
        }
        
        # 백테스팅 실행
        for i in range(strategy_class(self.api, self.symbol, self.interval).get_min_data_length(), len(self.data)):
            current_data = self.data.iloc[:i+1]
            current_price = current_data.iloc[-1]["close"]
            
            # 전략 인스턴스 생성
            strategy = strategy_class(self.api, self.symbol, self.interval)
            strategy.data = current_data.copy()
            strategy.calculate_indicators()
            signal = strategy.calculate_signal()
            
            portfolio_value = capital + position * current_price
            
            # 시그널 처리
            if signal == "buy" and capital > 0:
                # 매수 (전체 자본 사용)
                position = capital / current_price
                trades.append({
                    "datetime": current_data.index[-1],
                    "type": "buy",
                    "price": current_price,
                    "amount": position,
                    "value": capital,
                })
                logger.info(f"매수 신호: {current_data.index[-1]}, 가격: {current_price}, 수량: {position:.8f}")
                capital = 0
                
            elif signal == "sell" and position > 0:
                # 매도 (전체 포지션 청산)
                capital = position * current_price
                trades.append({
                    "datetime": current_data.index[-1],
                    "type": "sell",
                    "price": current_price,
                    "amount": position,
                    "value": capital,
                })
                logger.info(f"매도 신호: {current_data.index[-1]}, 가격: {current_price}, 수량: {position:.8f}")
                position = 0
                
            # 결과 저장
            results["datetime"].append(current_data.index[-1])
            results["price"].append(current_price)
            results["capital"].append(capital)
            results["position"].append(position)
            results["signal"].append(signal)
            results["portfolio_value"].append(portfolio_value)
            
        # 백테스팅 결과 DataFrame 생성
        self.results = pd.DataFrame(results)
        self.results.set_index("datetime", inplace=True)
        
        # 최종 포트폴리오 가치
        final_portfolio_value = self.results["portfolio_value"].iloc[-1]
        
        # 백테스팅 결과 계산
        total_return = (final_portfolio_value / self.initial_capital - 1) * 100
        
        # 연평균 수익률 계산
        days = (self.end_date - self.start_date).days
        if days > 0:
            annual_return = (1 + total_return / 100) ** (365 / days) - 1
            annual_return *= 100
        else:
            annual_return = 0
            
        # 최대 낙폭 계산
        max_drawdown = 0
        peak = self.results["portfolio_value"].iloc[0]
        for value in self.results["portfolio_value"]:
            if value > peak:
                peak = value
            drawdown = (peak - value) / peak * 100
            if drawdown > max_drawdown:
                max_drawdown = drawdown
                
        # 승률 계산
        trades_df = pd.DataFrame(trades)
        if not trades_df.empty and len(trades_df) > 1:
            # 매수-매도 페어로 거래 결과 계산
            profit_trades = 0
            loss_trades = 0
            
            for i in range(0, len(trades_df) - 1, 2):
                if i + 1 < len(trades_df):
                    buy_trade = trades_df.iloc[i]
                    sell_trade = trades_df.iloc[i + 1]
                    
                    if buy_trade["type"] == "buy" and sell_trade["type"] == "sell":
                        profit = sell_trade["value"] - buy_trade["value"]
                        if profit > 0:
                            profit_trades += 1
                        else:
                            loss_trades += 1
            
            total_trades = profit_trades + loss_trades
            win_rate = profit_trades / total_trades * 100 if total_trades > 0 else 0
        else:
            win_rate = 0
            total_trades = 0
            
        # 결과 출력
        logger.info(f"백테스팅 결과:")
        logger.info(f"초기 자본: {self.initial_capital:,.0f} KRW")
        logger.info(f"최종 포트폴리오 가치: {final_portfolio_value:,.0f} KRW")
        logger.info(f"총 수익률: {total_return:.2f}%")
        logger.info(f"연평균 수익률: {annual_return:.2f}%")
        logger.info(f"최대 낙폭: {max_drawdown:.2f}%")
        logger.info(f"총 거래 횟수: {total_trades}")
        logger.info(f"승률: {win_rate:.2f}%")
        
        return {
            "initial_capital": self.initial_capital,
            "final_portfolio_value": final_portfolio_value,
            "total_return": total_return,
            "annual_return": annual_return,
            "max_drawdown": max_drawdown,
            "total_trades": total_trades,
            "win_rate": win_rate,
            "trades": trades,
            "results": self.results.to_dict(),
        }
        
    def plot(self, filename: Optional[str] = None):
        """
        백테스팅 결과 시각화

        Parameters
        ----------
        filename : Optional[str]
            저장할 파일 경로 (None인 경우 화면에 표시)
        """
        if self.results.empty:
            logger.error("백테스팅 결과가 없습니다.")
            return
            
        plt.figure(figsize=(15, 10))
        
        # 가격 차트
        ax1 = plt.subplot(2, 1, 1)
        ax1.plot(self.results.index, self.results["price"], label="가격")
        
        # 매수/매도 신호 표시
        buy_signals = self.results[self.results["signal"] == "buy"]
        sell_signals = self.results[self.results["signal"] == "sell"]
        
        ax1.scatter(buy_signals.index, buy_signals["price"], color="green", marker="^", s=100, label="매수")
        ax1.scatter(sell_signals.index, sell_signals["price"], color="red", marker="v", s=100, label="매도")
        
        ax1.set_title(f"{self.symbol} {self.strategy_name} 백테스팅 결과")
        ax1.set_ylabel("가격 (KRW)")
        ax1.legend()
        ax1.grid(True)
        
        # 포트폴리오 가치 차트
        ax2 = plt.subplot(2, 1, 2)
        ax2.plot(self.results.index, self.results["portfolio_value"], label="포트폴리오 가치")
        ax2.set_ylabel("가치 (KRW)")
        ax2.set_xlabel("날짜")
        ax2.legend()
        ax2.grid(True)
        
        plt.tight_layout()
        
        if filename:
            plt.savefig(filename)
            logger.info(f"차트 저장: {filename}")
        else:
            plt.show() 