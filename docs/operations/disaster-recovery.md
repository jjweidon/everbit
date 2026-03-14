# 재해 복구 (Disaster Recovery)

Status: **Ready for Operation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

운영 구조는 **Backend(OCI E2.1.Micro) + DB/Auth/Storage(Supabase)**이다.  
VM 장애 시 API 레이어만 다운되며, DB는 Supabase 측 가용성에 따른다. v2는 HA를 목표로 하지 않지만, **복구 가능성**은 반드시 확보한다.

---

## 1. 목표(RPO/RTO)

- RPO(데이터 손실 허용): 24시간 이내(최소), 목표는 1시간 이내(향후)
- RTO(복구 시간): 2~6시간(단일 VM 재구축 포함)

---

## 2. 복구 대상(우선순위)

### P0(반드시)
- **Supabase(PostgreSQL) 데이터** — 주문/체결/포지션/설정/키 암호문/Outbox. Supabase PITR/백업 정책 확인.
- DNS/TLS 재구성(재발급 가능)

### P1(가능하면)
- Grafana 대시보드/설정
- Jenkins job 설정(필요 시)

---

## 3. 백업 정책(최소)

### 3.1 DB(Supabase) 백업(필수)
- Supabase 프로젝트에서 **PITR(Point-in-Time Recovery)** 또는 정기 백업 활성화 권장(실거래 운영 시).
- 주기: Supabase 제공 백업 정책 + 필요 시 주기적 `pg_dump` 외부 보관(7~14일).
- VM에 DB가 없으므로 VM 디스크 백업만으로는 DB 복구 불가.

### 3.2 VM/볼륨 스냅샷(옵션)
- 주 1회(가능하면). 애플리케이션/설정 복구용. DB는 Supabase에 있음.

---

## 4. 복구 절차(표준)

### 4.1 VM 완전 유실
1) 새 OCI VM 생성(**E2.1.Micro**, 동일 리전)
2) **Swap 2GB** 설정 + swappiness=10
3) NSG/iptables 적용(80/443, 22 제한)
4) Docker 설치
5) `/etc/everbit/everbit.env` 재생성(시크릿은 백업하지 않음) — Supabase 연결 정보 포함
6) repo clone + compose(prod) 준비 — **DB 서비스 없음**
7) DNS 연결(또는 Reserved IP 재연결)
8) TLS 재발급
9) 서버 기동(Supabase는 이미 운영 중이므로 별도 DB 기동 없음)
10) 기능 검증(로그인/키 복호화/대시보드)

### 4.2 DB(Supabase) 손상/복구
1) Kill Switch OFF
2) Supabase 대시보드에서 PITR 또는 백업으로 복구
3) 서버 기동 후 정합성 점검
4) Kill Switch ON

---

## 5. 복구 후 검증 체크리스트
- [ ] https://api.everbit.kr 정상
- [ ] Kakao 로그인 정상
- [ ] Upbit 키 복호화 및 검증 정상
- [ ] 주문 파이프라인 정상(테스트 모드/드라이런 권장)
- [ ] 백업 스케줄 재활성화

---

## 6. 운영 규칙
- 최소 분기 1회 복구 리허설을 수행한다(Supabase 백업 복원 또는 VM 재구축 중 하나).
