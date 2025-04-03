#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Everbit 트레이딩 봇 메인 엔트리 포인트
"""

import argparse
import logging
import os
import sys
import time
from pathlib import Path

from dotenv import load_dotenv

# 현재 스크립트 디렉토리를 파이썬 모듈 검색 경로에 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

# PyJWT 관련 테스트 코드 제거

# 로거 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger(__name__)

# .env 파일 로드
load_dotenv()


def parse_args():
    """
    명령줄 인수 파싱
    """
    parser = argparse.ArgumentParser(description="Everbit 트레이딩 봇")
    parser.add_argument(
        "--strategy",
        type=str,
        default="bollinger_rsi",
        help="사용할 트레이딩 전략 (ma_crossover, bollinger_rsi)",
    )
    parser.add_argument("--backtest", action="store_true", help="백테스팅 모드 활성화")
    parser.add_argument(
        "--start", type=str, default="2023-01-01", help="백테스팅 시작일 (YYYY-MM-DD)"
    )
    parser.add_argument(
        "--end", type=str, default="2023-12-31", help="백테스팅 종료일 (YYYY-MM-DD)"
    )
    parser.add_argument("--symbol", type=str, default="KRW-BTC", help="거래 심볼")
    parser.add_argument("--interval", type=str, default="minute60", help="데이터 간격")
    parser.add_argument("--amount", type=float, default=1000000.0, help="초기 투자 금액 (KRW)")
    return parser.parse_args()


def run_backtest(args):
    """
    백테스팅 실행
    """
    from backtest.runner import BacktestRunner

    logger.info(f"백테스팅 시작: 전략 '{args.strategy}', 기간 {args.start} ~ {args.end}")
    runner = BacktestRunner(
        strategy_name=args.strategy,
        symbol=args.symbol,
        start_date=args.start,
        end_date=args.end,
        interval=args.interval,
        initial_capital=args.amount,
    )
    result = runner.run()
    logger.info(f"백테스팅 완료: 최종 수익률 {result.get('total_return', 0):.2f}%")
    return result


def run_trading_bot(args):
    """
    실시간 트레이딩 봇 실행
    """
    from strategies.factory import create_strategy
    from data.upbit_api import UpbitAPI

    logger.info(f"트레이딩 봇 시작: 전략 '{args.strategy}', 심볼 {args.symbol}")

    # Upbit API 연결
    upbit_access_key = os.getenv("UPBIT_ACCESS_KEY")
    upbit_secret_key = os.getenv("UPBIT_SECRET_KEY")

    if not upbit_access_key or not upbit_secret_key:
        logger.error("Upbit API 키가 설정되지 않았습니다. .env 파일을 확인하세요.")
        sys.exit(1)

    api = UpbitAPI(upbit_access_key, upbit_secret_key)
    strategy = create_strategy(args.strategy, api, args.symbol, args.interval)

    try:
        while True:
            strategy.update()
            strategy.execute()
            logger.info(f"전략 업데이트 완료, 다음 업데이트까지 대기 중...")
            time.sleep(60)  # 1분마다 업데이트
    except KeyboardInterrupt:
        logger.info("사용자에 의해 트레이딩 봇이 중지되었습니다.")
    except Exception as e:
        logger.error(f"트레이딩 봇 실행 중 오류 발생: {e}")
        raise


def main():
    """
    메인 함수
    """
    args = parse_args()

    if args.backtest:
        run_backtest(args)
    else:
        run_trading_bot(args)


if __name__ == "__main__":
    main() 