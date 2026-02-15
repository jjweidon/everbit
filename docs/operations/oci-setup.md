# OCI Always Free 인프라 세팅 (단일 VM 올인원)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

- **Oracle Cloud Infrastructure(OCI) Always Free** 기반으로 `api.everbit.kr` 운영 VM을 구성한다.
- 비용 상한: **$0 (Always Free 범위 내)** 를 기본으로 하며, 의도치 않은 과금이 발생하지 않도록 가드레일을 문서로 고정한다.
- 단일 VM에 Nginx + Spring + Postgres + Redis + Kafka + Prometheus/Grafana(+ Jenkins 선택)를 올린다.

---

## 2. Always Free 리소스/비용 가드레일

### 2.1 Compute (Ampere A1)
Always Free에는 Arm 기반 Ampere A1 Compute가 포함되며, 월 **3,000 OCPU-hours / 18,000 GB-hours**가 무료로 제공된다.

- VM.Standard.A1.Flex 기준으로 **4 OCPU / 24GB**를 “한 달 내내” 돌려도 이 범위 안으로 들어간다.
  - 4 OCPU * 24h * 30일 = 2,880 OCPU-hours
  - 24GB * 24h * 30일 = 17,280 GB-hours

레퍼런스:
- Oracle Cloud Free Tier(Always Free 설명): https://www.oracle.com/kr/cloud/free/
- Oracle Cloud Price List(A1 free hours): https://www.oracle.com/kr/cloud/price-list/

### 2.2 Storage
- Always Free에 블록 볼륨/오브젝트 스토리지가 포함된다(용량 제한 내).
- 운영 정책:
  - **DB 데이터(특히 주문/체결/키 암호문)는 반드시 별도 볼륨 또는 명확한 백업 절차로 보호**
  - **VM 재생성 가능**을 기본 전제로 둔다(= 복구 문서가 있어야 한다)

---

## 3. 가입(계정 생성) 체크리스트

OCI Free Tier 가입에서 가장 많이 막히는 포인트를 “규칙”으로 고정한다.

### 3.1 1인 1계정(중요)
- 1인당 1개의 Free Trial/Always Free 계정만 허용된다.
- 여러 계정 시도는 계정 정지/제한 리스크가 있다.

레퍼런스:
- https://www.oracle.com/cloud/free/
- https://www.oracle.com/cloud/free/faq/

### 3.2 결제수단 규칙(중요)
Oracle Free Tier는 신원 확인을 위해 카드 인증이 필요하다.

- 허용: 신용카드, “신용카드처럼 동작하는” 체크카드
- 불가: PIN 입력이 필요한 debit, virtual/단회용/선불(prepaid) 카드

레퍼런스:
- Oracle Free Tier FAQ(What payment methods does Oracle accept?): https://www.oracle.com/cloud/free/faq/

### 3.3 가입 실패를 줄이는 운영 팁(현실)
- VPN/프록시 사용 금지(위치/신원 마스킹으로 실패하는 케이스가 많음)
- 카드 청구지 주소/이름은 **은행 등록 정보와 일치**
- 인증용 승인(authorization hold)이 발생할 수 있고, 일반적으로 며칠 내 해제됨(FAQ에 명시)

---

## 4. 리전/용량 이슈 대응

Always Free는 **capacity limit**이 존재한다. 리전에 따라 A1 인스턴스 생성이 막히는 케이스가 있다.

정책:
- A1이 막히면 같은 국가/인접 리전으로 우회 시도
- “어디서도 생성 불가”면 우선 x86 micro 2개(Always Free)로 분산(임시)하거나, $8 이하 저가 VPS로 fallback을 검토(ADR로 남김)

레퍼런스:
- Free Tier FAQ(availability subject to capacity limits): https://www.oracle.com/cloud/free/

---

## 5. 네트워크(VCN) 표준

### 5.1 기본 구성
- VCN 1개
- Public Subnet 1개 (API 서버는 Public IP 필요)
- Internet Gateway(IGW) + Route Table

### 5.2 Security Lists vs NSG
- Security List: subnet 단위 규칙
- NSG: instance(VNIC) 단위 규칙

운영 표준:
- **NSG를 우선 사용**(인스턴스 단위로 규칙을 고정하기 쉬움)
- Security List는 기본 허용을 최소화

레퍼런스:
- OCI Security Rules: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm
- Security Lists: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securitylists.htm

---

## 6. Ingress 포트 정책(OCI 레벨)

### 6.1 인터넷 공개 포트(필수)
- 80/tcp: HTTP (Let's Encrypt HTTP-01 + 443 리다이렉트)
- 443/tcp: HTTPS (API)

### 6.2 SSH(권장)
- 22/tcp: SSH
  - **Source CIDR는 반드시 본인 고정 IP로 제한**
  - 고정 IP가 어렵다면: (대안) Cloudflare Zero Trust / Tailscale 같은 “접근 레이어”를 도입하고 22를 닫는 방향을 검토(추후)

### 6.3 금지(절대 공개 금지)
- 5432(Postgres), 6379(Redis), 9092(Kafka), 9090(Prometheus), 3000(Grafana), Jenkins 포트
- 이유: 단일 VM에서 해당 포트를 직접 공개하면 공격면이 급증하고, 운영 사고 시 피해 범위가 커진다.

---

## 7. OS 방화벽(OCI Ubuntu 이미지 주의사항)

OCI에서 Ubuntu 이미지는 “UFW로만” 방화벽을 편집하면 문제가 생길 수 있다.

- Oracle 문서/가이드에는 Ubuntu 이미지에서 UFW 사용을 권장하지 않는 내용이 있다.
- 또한, Ubuntu 이미지에서 포트 오픈은 `iptables` 설정 파일(`/etc/iptables/rules.v4`, `/etc/iptables/rules.v6`) 편집이 필요하다고 Oracle 개발자 블로그가 설명한다.

레퍼런스:
- Compute Best Practices(Do not use UFW…): https://docs.oracle.com/en-us/iaas/Content/Compute/References/bestpracticescompute.htm
- OCI Ubuntu 네트워크 오픈(iptables rules.v4/v6): https://blogs.oracle.com/developers/enabling-network-traffic-to-ubuntu-images-in-oracle-cloud-infrastructure

### 7.1 (권장) 포트 오픈 절차(80/443/22)
아래는 “OS 레벨에서 포트가 막혀서 외부 접근이 안 되는” 케이스를 해결하기 위한 표준 절차다.

1) 현재 룰 확인
```bash
sudo iptables -S | head -n 200
sudo ip6tables -S | head -n 200
```

2) iptables-persistent 설치(없으면)
```bash
sudo apt update
sudo apt install -y iptables-persistent
```

3) `/etc/iptables/rules.v4`에서 REJECT 보다 위에 허용 룰을 추가
예시(개념):
- 80/443는 전체 허용
- 22는 내 IP만 허용

```text
-A INPUT -p tcp --dport 80 -j ACCEPT
-A INPUT -p tcp --dport 443 -j ACCEPT
-A INPUT -p tcp --dport 22 -s <MY_PUBLIC_IP>/32 -j ACCEPT
```

4) 적용
```bash
sudo iptables-restore < /etc/iptables/rules.v4
sudo ip6tables-restore < /etc/iptables/rules.v6
```

주의:
- OCI 이미지 기본 룰(필수 룰)을 삭제하지 않는다.
- “파일 맨 끝에만 룰 추가”는 무시될 수 있다(Oracle 블로그 내용 참고).

---

## 8. VM 하드닝(최소)

### 8.1 SSH
- 비밀번호 로그인 금지(키 인증만)
- root 직접 로그인 금지
- sshd 설정 변경 후 반드시 연결 확인

### 8.2 패치
- OS 보안 업데이트 주기 고정(주 1회 또는 자동 업데이트)

### 8.3 시간/로그
- `TZ=Asia/Seoul` 고정
- 로그 로테이션 활성화(디스크 폭주 방지)

---

## 9. Day-0 체크리스트

- [ ] A1.Flex(4 OCPU/24GB) 생성 완료
- [ ] Public IP 확인 + (가능하면) Reserved Public IP로 고정
- [ ] NSG/보안목록에 80/443 허용, 22는 내 IP만 허용
- [ ] OS iptables에서 80/443/22 허용(OCI Ubuntu 케이스)
- [ ] Docker 설치
- [ ] `/etc/everbit/everbit.env` 생성(권한 600)
- [ ] docker compose(prod) 구동
- [ ] DNS: `api.everbit.kr` → VM Public IP A 레코드 적용
- [ ] TLS 발급/갱신 자동화

다음 문서로 이동:
- `docs/operations/nginx-tls.md`
- `docs/operations/admin-surface-policy.md`
- `docs/operations/deploy.md`
