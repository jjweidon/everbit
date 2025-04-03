#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
백테스트 실행기 모듈
"""

import logging
import os
import sys
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Union

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from dateutil.parser import parse

# 모듈 경로 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.dirname(current_dir)
sys.path.insert(0, os.path.dirname(src_dir))

from data.upbit_api import UpbitAPI
from strategies.factory import create_strategy, STRATEGIES

logger = logging.getLogger(__name__)


class BacktestRunner:
    """
    백테스트 실행기 클래스
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
            백테스트 시작일 (YYYY-MM-DD)
        end_date : str
            백테스트 종료일 (YYYY-MM-DD)
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
        # 백테스트용 더미 API 키 사용 - 더 견고한 방식으로 수정
        dummy_access_key = "DUMMY_ACCESS_KEY_FOR_BACKTEST"
        dummy_secret_key = "DUMMY_SECRET_KEY_FOR_BACKTEST" 
        self.api = UpbitAPI(dummy_access_key, dummy_secret_key)  # 백테스트 API
        self.data = pd.DataFrame()
        self.results = pd.DataFrame()

    def _load_data(self) -> pd.DataFrame:
        """
        백테스트 데이터 로드

        Returns
        -------
        pd.DataFrame
            캔들 데이터
        """
        try:
            # API로 데이터 로드
            try:
                data = self.api.get_candles(self.symbol, self.interval, count=200)
                if not data.empty:
                    # 시작일과 종료일로 필터링
                    mask = (data.index >= self.start_date) & (data.index <= self.end_date)
                    data = data.loc[mask]
                    return data
            except Exception as e:
                logger.warning(f"API에서 데이터 로드 실패: {e}. 모의 데이터를 사용합니다.")
            
            # API 로드 실패시 모의 데이터 생성
            logger.info("백테스트를 위한 모의 데이터를 생성합니다.")
            return self._generate_mock_data()
            
        except Exception as e:
            logger.error(f"데이터 로드 중 오류 발생: {e}")
            return pd.DataFrame()
    
    def _generate_mock_data(self) -> pd.DataFrame:
        """
        백테스트를 위한 모의 데이터 생성
        
        Returns
        -------
        pd.DataFrame
            모의 캔들 데이터
        """
        # 날짜 범위 설정
        date_range = []
        if self.interval.startswith('minute'):
            # 분 캔들
            minutes = int(self.interval[6:])
            current = self.start_date
            while current <= self.end_date:
                if 9 <= current.hour < 23:  # 시장 시간만
                    date_range.append(current)
                current += timedelta(minutes=minutes)
        else:
            # 일/주/월 캔들
            if self.interval == 'day':
                freq = 'D'
            elif self.interval == 'week':
                freq = 'W'
            elif self.interval == 'month':
                freq = 'M'
            date_range = pd.date_range(start=self.start_date, end=self.end_date, freq=freq)
        
        # 초기 가격 설정 (BTC 기준 약 2천만원대)
        base_price = 20000000.0
        
        # 랜덤 가격 생성
        np.random.seed(42)  # 재현 가능한 결과를 위한 시드
        
        # 상승 추세 + 변동성 시뮬레이션
        price_changes = np.random.normal(0.0005, 0.02, size=len(date_range))  # 평균 상승률 0.05%, 표준편차 2%
        prices = [base_price]
        
        for change in price_changes:
            next_price = prices[-1] * (1 + change)
            prices.append(next_price)
        
        prices = prices[1:]  # 첫 번째 요소(초기값) 제거
        
        # 캔들 데이터 생성
        data = []
        for i, date in enumerate(date_range):
            price = prices[i]
            high = price * (1 + np.random.uniform(0, 0.01))  # 최고가
            low = price * (1 - np.random.uniform(0, 0.01))   # 최저가
            open_price = price * (1 + np.random.uniform(-0.005, 0.005))  # 시가
            volume = np.random.uniform(0.5, 5.0) * 100  # 거래량
            
            data.append({
                'datetime': date,
                'open': open_price,
                'high': high,
                'low': low,
                'close': price,
                'volume': volume,
                'value': volume * price
            })
        
        # DataFrame 생성
        df = pd.DataFrame(data)
        df.set_index('datetime', inplace=True)
        
        logger.info(f"모의 데이터 생성 완료: {len(df)} 캔들")
        return df

    def run(self) -> Dict:
        """
        백테스트 실행

        Returns
        -------
        Dict
            백테스트 결과
        """
        logger.info(f"{self.strategy_name} 전략 백테스트 시작")
        logger.info(f"기간: {self.start_date.strftime('%Y-%m-%d')} ~ {self.end_date.strftime('%Y-%m-%d')}")
        
        # 데이터 로드
        self.data = self._load_data()
        if self.data.empty:
            logger.error("백테스트 데이터가 없습니다.")
            return {"error": "데이터 로드 실패"}
            
        # 전략 생성
        strategy_class = STRATEGIES.get(self.strategy_name)
        if not strategy_class:
            logger.error(f"지원되지 않는 전략: {self.strategy_name}")
            return {"error": f"지원되지 않는 전략: {self.strategy_name}"}
            
        # 백테스트 준비
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
        
        # 전략 파라미터 설정
        strategy_params = {}
        if self.strategy_name == "bollinger_rsi":
            strategy_params = {
                "partial_ratio": 0.5  # 부분 매도 비율 (50%)
            }
            
        # 백테스트용 전략 인스턴스 생성
        strategy_instance = strategy_class(self.api, self.symbol, self.interval, strategy_params)
        min_data_length = strategy_instance.get_min_data_length()
        
        # 매수 가격 추적 (손절 및 이익실현 계산용)
        buy_price = None
        partial_take_profit = False  # 첫 번째 이익실현 완료 여부
        
        # 백테스트 실행
        for i in range(min_data_length, len(self.data)):
            current_data = self.data.iloc[:i+1]
            current_price = current_data.iloc[-1]["close"]
            
            # 전략 인스턴스 생성
            strategy = strategy_class(self.api, self.symbol, self.interval, strategy_params)
            strategy.data = current_data.copy()
            strategy.position = "long" if position > 0 else None
            strategy.buy_price = buy_price
            strategy.partial_take_profit = partial_take_profit
            strategy.calculate_indicators()
            signal = strategy.calculate_signal()
            
            portfolio_value = capital + position * current_price
            
            # 시그널 처리
            if signal == "buy" and capital > 0:
                # 매수 (전체 자본의 30% 사용)
                buy_amount = capital * 0.3
                new_position = buy_amount / current_price
                position += new_position
                trades.append({
                    "datetime": current_data.index[-1],
                    "type": "buy",
                    "price": current_price,
                    "amount": new_position,
                    "value": buy_amount,
                })
                logger.info(f"매수 신호: {current_data.index[-1]}, 가격: {current_price}, 수량: {new_position:.8f}")
                capital -= buy_amount
                buy_price = current_price
                partial_take_profit = False
                
            elif signal == "sell" and position > 0:
                # 전체 매도
                sell_value = position * current_price
                trades.append({
                    "datetime": current_data.index[-1],
                    "type": "sell",
                    "price": current_price,
                    "amount": position,
                    "value": sell_value,
                })
                logger.info(f"매도 신호: {current_data.index[-1]}, 가격: {current_price}, 수량: {position:.8f}")
                capital += sell_value
                position = 0
                buy_price = None
                partial_take_profit = False
                
            elif signal == "partial_sell" and position > 0:
                # 부분 매도 (포지션의 50%)
                partial_ratio = strategy_params.get("partial_ratio", 0.5)
                sell_position = position * partial_ratio
                sell_value = sell_position * current_price
                trades.append({
                    "datetime": current_data.index[-1],
                    "type": "partial_sell",
                    "price": current_price,
                    "amount": sell_position,
                    "value": sell_value,
                })
                logger.info(f"부분 매도 신호: {current_data.index[-1]}, 가격: {current_price}, 수량: {sell_position:.8f}, 비율: {partial_ratio:.2%}")
                capital += sell_value
                position -= sell_position
                partial_take_profit = True
            
            # 결과 저장
            results["datetime"].append(current_data.index[-1])
            results["price"].append(current_price)
            results["capital"].append(capital)
            results["position"].append(position)
            results["signal"].append(signal)
            results["portfolio_value"].append(portfolio_value)
            
        # 백테스트 결과 DataFrame 생성
        self.results = pd.DataFrame(results)
        self.results.set_index("datetime", inplace=True)
        
        # 최종 포트폴리오 가치
        final_portfolio_value = self.results["portfolio_value"].iloc[-1]
        
        # 백테스트 결과 계산
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
        if not trades_df.empty:
            # 모든 거래 페어 계산
            profit_trades = 0
            loss_trades = 0
            
            # 각 매수-매도 페어 또는 매수-부분매도-매도 시퀀스 분석
            buy_indices = trades_df[trades_df["type"] == "buy"].index.tolist()
            
            for buy_idx in buy_indices:
                # 해당 매수 이후의 모든 매도 및 부분 매도 찾기
                sell_indices = trades_df[(trades_df.index > buy_idx) & 
                                         ((trades_df["type"] == "sell") | 
                                          (trades_df["type"] == "partial_sell"))].index.tolist()
                
                if sell_indices:
                    buy_trade = trades_df.iloc[buy_idx]
                    buy_price = buy_trade["price"]
                    buy_value = buy_trade["value"]
                    
                    # 해당 매수에 대한 총 매도 가치 계산
                    total_sell_value = 0
                    for sell_idx in sell_indices:
                        sell_trade = trades_df.iloc[sell_idx]
                        # 다음 매수 전까지의 매도만 고려
                        if sell_idx < len(trades_df) - 1 and trades_df.iloc[sell_idx + 1]["type"] == "buy":
                            break
                        total_sell_value += sell_trade["value"]
                    
                    # 수익 여부 판단
                    profit = total_sell_value - buy_value
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