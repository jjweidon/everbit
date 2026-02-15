# 재해 복구 (Disaster Recovery)

Status: **Ready for Operation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

단일 VM 올인원 구조는 VM 장애 시 전체 다운이 발생한다.  
v2는 HA를 목표로 하지 않지만, **복구 가능성**은 반드시 확보한다.

---

## 1. 목표(RPO/RTO)

- RPO(데이터 손실 허용): 24시간 이내(최소), 목표는 1시간 이내(향후)
- RTO(복구 시간): 2~6시간(단일 VM 재구축 포함)

---

## 2. 복구 대상(우선순위)

### P0(반드시)
- PostgreSQL 데이터(주문/체결/포지션/설정/키 암호문/Outbox)
- DNS/TLS 재구성(재발급 가능)

### P1(가능하면)
- Grafana 대시보드/설정
- Jenkins job 설정(필요 시)

---

## 3. 백업 정책(최소)

### 3.1 DB 덤프(필수)
- 주기: 하루 1회(최소)
- 보관: 7~14일
- 방법: `pg_dump` 논리 백업

권장:
- 백업을 VM 디스크에만 두지 말고 Object Storage 업로드를 검토한다(Always Free 범위 내).

### 3.2 볼륨 스냅샷(옵션)
- 주 1회(가능하면)
- 단, 스냅샷만으로 “복구”가 보장되지는 않는다. 복구 절차가 핵심이다.

---

## 4. 복구 절차(표준)

### 4.1 VM 완전 유실
1) 새 OCI VM 생성(동일 shape/리전)
2) NSG/iptables 적용(80/443, 22 제한)
3) Docker 설치
4) `/etc/everbit/everbit.env` 재생성(시크릿은 백업하지 않음)
5) repo clone + compose(prod) 준비
6) DNS 연결(또는 Reserved IP 재연결)
7) TLS 재발급
8) PostgreSQL 기동
9) DB restore(pg_restore/psql)
10) 서버 기동
11) 기능 검증(로그인/키 복호화/대시보드)

### 4.2 DB 손상
1) Kill Switch OFF
2) Postgres 중지
3) 최신 정상 백업으로 restore
4) 서버 기동 후 정합성 점검
5) Kill Switch ON

---

## 5. 복구 후 검증 체크리스트
- [ ] https://api.everbit.kr 정상
- [ ] Kakao 로그인 정상
- [ ] Upbit 키 복호화 및 검증 정상
- [ ] 주문 파이프라인 정상(테스트 모드/드라이런 권장)
- [ ] 백업 스케줄 재활성화

---

## 6. 운영 규칙
- 최소 분기 1회 restore 리허설을 수행한다(빈 DB에 restore만이라도).
