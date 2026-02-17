# UI/UX 디자인 컨셉 (Everbit v2)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- 퀀트 트레이딩 시스템으로서 “정밀함/신뢰/성능/차분함”을 전달한다.
- 기본은 **다크(Black/Charcoal)**, 포인트는 **형광 계열(Neon Green/Red/Yellow)** 로 “상태/리스크/변화”를 빠르게 인지하게 한다.
- 정보 밀도는 높되, UI는 과하지 않게. “차분한 HUD” 톤을 유지한다.

범위:
- 디자인 토큰(색/타이포/간격/테두리/그림자)
- 레이아웃/내비게이션
- 컴포넌트 스타일(버튼/칩/테이블/카드/폼)
- 데이터 시각화(차트/지표)
- 인터랙션(모션/피드백)
- 접근성(contrast/focus/reduced motion)

비목표:
- 브랜딩 로고/일러스트/마케팅 페이지
- 라이트 테마 완성(추후 옵션)

---

## 1. 비주얼 키워드

- **Modern / Minimal / Technical**: 과장된 그래픽 대신 규격화된 그리드와 정돈된 타이포
- **Dark Tactical**: 순수 블랙(#000) 대신 “딥 차콜” 계열로 눈 피로를 낮춤
- **Signal-Driven Accent**: 포인트 컬러는 “의미(상태/방향/위험)”에만 사용
- **Calm Performance**: 과도한 애니메이션 금지, 빠른 반응과 미세한 피드백만

---

## 2. 컬러 시스템(다크 베이스 + 형광 포인트)

원칙:
- 배경은 2~3단 레이어만 사용(너무 많은 그레이 금지)
- 포인트 컬러는 “의미 기반”으로 제한(장식용 금지)
- **GREEN=긍정/진행/수익**, **RED=위험/손실/중단**, **YELLOW=주의/대기/제한**

### 2.1 Neutral(기본)
- BG-0 (App background): `#0B0F14`
- BG-1 (Surface): `#0F1620`
- BG-2 (Elevated): `#121C28`
- Border (subtle): `#243244`
- Divider: `#1A2635`
- Text-Primary: `#E7EEF8`
- Text-Secondary: `#A8B3C2`
- Text-Tertiary: `#6D7A8D`

### 2.2 Accent(포인트)
- Neon Green (Up/Success): `#39FF88`
- Neon Red (Down/Danger): `#FF4D6D`
- Neon Yellow (Warn): `#FFD166`
- Cyan (Info/Link): `#3EC5FF`

### 2.3 상태 색 적용 규칙
- 상태 칩/배지:
  - RUNNING: Green
  - STOPPED/KILL: Red
  - SUSPENDED: Yellow
  - UNKNOWN: Yellow + 아이콘(물음표/경고)
- 수익/손실 숫자:
  - + : Green
  - - : Red
- 버튼:
  - Primary: Green(트레이딩 시작/저장)
  - Destructive: Red(키 폐기/중단)
  - Secondary: Neutral

---

## 3. 타이포그래피

원칙:
- 숫자/지표 가독성 우선(단위/소수/자리수 정렬)
- 제목은 과하게 크지 않게(정보 밀도 유지)

권장 폰트:
- KR: Pretendard(또는 Noto Sans KR)
- EN/Number: Inter

스케일(권장):
- H1: 20/28 (Dashboard Title)
- H2: 16/24 (Section)
- Body: 14/20 (기본 텍스트)
- Caption: 12/16 (메타/시간/부가)

숫자 표기:
- PnL/수익률/잔고는 고정 자리수(예: 소수 2자리)
- 테이블 컬럼은 monospaced 느낌(가능하면 `tabular-nums`)

---

## 4. 레이아웃/그리드

원칙:
- “대시보드형” 레이아웃: 좌측 내비 + 본문
- 최대 폭 제한으로 밀도 제어(너무 넓게 퍼지지 않게)

권장:
- Layout: `Sidebar (240~280px) + Content`
- Content max width: 1200~1440
- Spacing: 4px 기반(4/8/12/16/24/32)

페이지 구조(기본):
- 상단: 전역 상태(Kill Switch, 연결 상태)
- 본문: 카드 그리드(상태/주문/잔고/손익)
- 하단: 로그/이벤트 타임라인(선택)

---

## 5. 컴포넌트 스타일 가이드

### 5.1 Surface/Card
- 배경: BG-1 또는 BG-2
- Border: 1px Border(subtle)
- Radius: 10~12
- Shadow: 아주 약하게(깊은 그림자 금지)

### 5.2 Button
- Primary: Green 배경 + Dark 텍스트(또는 반전)
- Secondary: BG-2 + Border
- Destructive: Red
- Disabled: 투명도 + 커서

### 5.3 Form
- Input 배경: BG-2
- Border: Border(subtle)
- Focus: Cyan/Green outline(접근성)
- Error: Red + helper text

### 5.4 Badge/Chip
- 높이: 24~28
- 텍스트: 12~13
- 색상은 상태에 의미 부여(2.3 규칙)

### 5.5 Table
- 헤더는 BG-2, 본문 row hover는 BG-2로 살짝 상승
- 숫자 컬럼은 우측 정렬
- 중요한 컬럼(시장/상태/PnL)은 고정 폭 또는 강조

---

## 6. 데이터 시각화(차트/지표)

원칙:
- 차트는 “정보”가 목적. 장식/그라데이션 최소화.
- 색으로만 의미를 전달하지 않는다(아이콘/레이블 병행).

권장 규격:
- 캔들/라인 색:
  - 상승: Green
  - 하락: Red
- 보조선(Grid): Border(subtle)보다 약하게
- Tooltip:
  - BG-2 + Border
  - 숫자는 tabular-nums

지표 카드:
- KPI(잔고, 수익률, MDD 등)는 큰 숫자 + 작은 라벨
- 변화율은 +/− 색상과 화살표

---

## 7. 인터랙션/모션

원칙:
- 빠르고 짧게. “반응한다”는 느낌만 주고 과하게 움직이지 않는다.

권장:
- hover: 150ms
- open/close: 180~220ms
- skeleton/loading: 미세한 shimmer(과도 금지)
- reduce motion:
  - OS 설정에 따라 애니메이션 최소화

피드백:
- 저장 성공: Green toast(짧게)
- 위험 액션: confirm modal(Destructive)
- 네트워크 오류: Red toast + 재시도 버튼

---

## 8. 접근성(필수)

- 텍스트 대비: 최소 WCAG AA 수준 목표
- Focus ring: 키보드 탐색 시 반드시 표시
- 색상만으로 상태를 표현하지 않기:
  - 상태 칩에 아이콘/텍스트 병행
- 중요한 토글(Kill Switch)은 실수 방지:
  - 토글 + 1회 확인(옵션)
  - 상태 변화 즉시 로그/타임스탬프 표시

---

## 9. Tailwind 적용(권장 토큰화)

원칙:
- 색상을 코드에 하드코딩하지 않고 CSS 변수 기반으로 토큰화한다.

예시(CSS 변수):
```css
:root {
  --bg-0: 11 15 20;
  --bg-1: 15 22 32;
  --bg-2: 18 28 40;
  --border: 36 50 68;
  --text-1: 231 238 248;
  --text-2: 168 179 194;
  --green: 57 255 136;
  --red: 255 77 109;
  --yellow: 255 209 102;
  --cyan: 62 197 255;
}
```

Tailwind 예시(의미 기반 클래스):
- `bg-bg0`, `bg-surface`, `text-primary`, `text-secondary`
- `text-up`, `text-down`, `text-warn`

권장: 디자인 토큰은 `shared/ui/tokens` 또는 `styles/tokens.css`로 고정한다.

---

## 10. 화면별 UX 컨셉(핵심)

### 10.1 로그인
- 단일 버튼(카카오 로그인)
- 로그인 후 즉시 “보안/키 등록” onboarding로 유도

### 10.2 키 관리(Upbit)
- 키 등록 폼은 최소 필드 + “검증” 결과 즉시 표시
- 폐기(Destructive)는 confirm + 후속 안내

### 10.3 트레이딩 실행
- 상단에 Kill Switch(전역) 고정
- 전략/마켓 상태는 칩으로 즉시 가시화(RUNNING/SUSPENDED)

### 10.4 대시보드
- 1열: 실행 상태/최근 이벤트
- 2열: 잔고/손익
- 3열: 최근 주문/체결
- 확장 영역은 접기/펼치기로 밀도 관리

---

## 11. Done 체크

- [ ] 색상 토큰이 코드에 하드코딩되지 않음
- [ ] RUNNING/STOPPED/SUSPENDED/UNKNOWN 상태가 UI에서 일관 표시
- [ ] 테이블 숫자 정렬/가독성 확보(tabular-nums)
- [ ] Kill Switch UX가 실수 방지 형태
