# 배포 표준 (단일 VM + Docker Compose)

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- 운영 배포를 “재현 가능”하게 만든다.
- 롤백이 가능한 형태로 배포한다(Git SHA 기반).

---

## 1. 기본 원칙

- 운영 배포는 `v2` 브랜치 기준이며, 이미지 태그는 **Git SHA**를 사용한다.
- 운영 compose는 `compose.yaml + compose.prod.yaml` 조합을 사용한다.
- 외부 노출은 nginx(80/443)만 허용한다.

---

## 2. 배포 절차(표준)

### 2.1 서버 접속
```bash
ssh -i ~/.ssh/<YOUR_SSH_KEY> ubuntu@api.everbit.kr
```

### 2.2 repo 업데이트
```bash
cd ~/everbit
git fetch --all
git checkout v2
git pull
```

### 2.3 이미지 갱신(예: GHCR 사용 시)
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml pull
```

### 2.4 재기동
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml up -d
```

### 2.5 헬스 체크
```bash
curl -I https://api.everbit.kr
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml ps
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml logs --tail=200 server
```

---

## 3. 롤백 절차

- “이전 SHA 태그”로 compose가 참조하는 이미지 태그를 되돌린 뒤 `pull + up -d`를 수행한다.
- 롤백 후 동일 헬스 체크를 수행한다.

---

## 4. 운영 안전장치

- 배포 직전:
  - Kill Switch OFF(권장) → 배포 → 안정화 후 ON
- 배포 중 오류:
  - 즉시 롤백
  - DB 마이그레이션이 포함된 경우, 롤백 시 역호환 여부 확인(ADR/마이그레이션 정책 필요)

---

## 5. Done

- [ ] 배포한 SHA가 기록되어 있음(릴리즈 노트/로그)
- [ ] API 헬스 정상
- [ ] Admin 포트 외부 노출 없음
