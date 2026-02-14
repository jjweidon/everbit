# Git 브랜치/커밋 전략

## 원칙: 구현보다 문서가 먼저

레포지토리에 "문서 우선(구현보다 문서가 먼저)" 게이트를 명시적으로 남긴다. 이 문서가 그 기준선이다.

## 브랜치 전략

| 브랜치 | 용도 | 비고 |
|--------|------|------|
| `v1` | v1 안정 버전 고정 | 배포/운영 기준선 |
| `v2` | v2 개발 | v2 모든 작업은 여기서만 |
| `main` | (선택) 최신 안정 버전 | 필요 시 v1/v2 머지 후 태그 |

### 태그

* `v1.0.0`: v1 최종 안정 릴리스
* v2 작업은 `v2` 브랜치에서 진행, 필요 시 `v2.0.0` 등 태그 부여

### 브랜치 생성/고정 절차

1. **v1 고정**: `v1.0.0` 태그 + `v1` 브랜치 생성(이미 있다면 유지)
2. **v2 작업**: `v2` 브랜치에서만 개발
3. v1에 대한 핫픽스가 필요한 경우: `v1`에서 브랜치 분기 후 수정, 태그 갱신

## 커밋 전략

### 1. 문서 우선(필수)

* **새 기능/변경 사항**: 구현 코드 커밋 전에 관련 문서(요구사항/아키텍처/ADR/전략 스펙)를 먼저 커밋한다.
* **문서가 없으면**: 구현 PR/머지 전에 문서 보완 후 커밋한다.
* 예: Kafka 토픽 추가 → `docs/architecture/kafka-topics.md` 업데이트 → `docs/adr/XXXX-kafka-topic-xxx.md`(필요 시) → 구현

### 2. 커밋 메시지 규칙

```
<type>: <subject>

[optional body]
```

* **type**: `docs`, `feat`, `fix`, `refactor`, `test`, `chore` 등
* **docs**: 문서 추가/수정은 반드시 `docs:` 프리픽스 사용
* **subject**: 50자 이내, 명령형 (예: "Add FRD for Kakao OAuth2")

### 3. 커밋 단위

* **문서**: 관련 문서를 묶어서 1커밋 (예: "docs: Add v2 FRD/NFR and git strategy")
* **구현**: 기능 단위로 분리, 가능하면 작은 단위로

## 문서 트리와 커밋

`.gitignore`에서 `docs/ignore/`만 제외한다. 나머지 `docs/` 하위 문서는 모두 커밋 대상이다.

* `docs/ignore/`: 개인정보·비공개 문서용 (Git/AI 인덱싱 제외)
* `docs/requirements/`, `docs/architecture/`, `docs/adr/` 등: 커밋 필수

## v2 킥오프 체크리스트

1. [x] `docs/` 트리 생성
2. [x] FRD (`docs/requirements/functional.md`) 작성
3. [x] NFR (`docs/requirements/non-functional.md`) 작성
4. [x] Git 전략 문서 (`docs/operations/git-strategy.md`) 작성
5. [x] ADR 5개 작성(짧게라도)
6. [ ] 문서 세트 최초 커밋
