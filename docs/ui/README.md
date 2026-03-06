# UI 문서 (Everbit v2)

이 디렉터리는 **Everbit v2 UI/UX 구현을 위한 SoT**를 제공한다.

## 파일 구성

- `everbit_ui_impl_spec.md`
  - 라우팅(Next App Router), 주요 페이지 구성, 컴포넌트/상태 표기 규칙, UX 안전 규칙(UNKNOWN/SUSPENDED/Kill Switch)
  - 반드시 `docs/architecture/order-pipeline.md`와 같이 읽는다.

- `tokens.css`
  - 다크 뉴트럴 팔레트(blue cast 최소) 토큰
  - 배경 기준:
    - deepest: `#0A0A0A`
    - base: `#111214`
    - surface: `#17181B`

- `mockups/`
  - 주요 페이지 목업 PNG
  - 구현 검증(레이아웃/밀도/리스크 노출) 기준으로 사용

## 토큰 동기화 규칙

- SoT: `docs/ui/tokens.css`
- 런타임 반영: `client/src/styles/tokens.css`
- `client/src/app/globals.css`에서 import:

```css
@import "../styles/tokens.css";
```

> 토큰 변경은 문서(SoT) 먼저 갱신한 뒤, 런타임 파일로 복사/동기화한다.

## Cursor 사용

- Cursor에서 v2를 덮어쓰기 셋업할 때는 `docs/prompt-pack/00_README.md`부터 진행한다.
- Rules 파일: `.cursor/rules/everbit-v2.md`
