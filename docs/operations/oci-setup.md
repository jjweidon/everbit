# OCI 세팅 (Oracle Cloud Free Tier, Day-0)

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- OCI Always Free 단일 VM에 everbit v2 운영 환경을 만든다.
- 과금 리스크를 최소화하고, “네트워크/포트 정책”을 Day-0에서 고정한다.

---

## 1. 가입 전 체크(실패 방지)

- VPN/프록시 OFF
- 브라우저 확장(광고차단/추적차단)로 가입 폼이 깨지면 비활성화
- 결제수단은 **신용카드 또는 신용카드처럼 동작하는 체크카드** 권장
- 1인 1계정 원칙 준수

참고:
- Free tier FAQ: https://www.oracle.com/cloud/free/faq/
- 가입 문서: https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/signingup_topic-Sign_Up_for_Free_Oracle_Cloud_Promotion.htm

---

## 2. 인스턴스 생성(Always Free)

권장:
- Shape: `VM.Standard.A1.Flex`
- OCPU/Memory: Always Free 범위 내에서 설정(예: 4 OCPU / 24GB)
- OS: Ubuntu LTS

팁:
- 리전/가용영역에 따라 A1 capacity가 없을 수 있다. 생성 실패 시:
  - 다른 AD 시도
  - 리전 변경(가능하면)

---

## 3. 네트워크(VCN + NSG) 인바운드 룰(필수)

원칙:
- 외부 공개는 80/443만
- SSH(22)는 **내 IP /32**만

NSG(Security Rules) 예시:
- Ingress allow:
  - TCP 80 from 0.0.0.0/0
  - TCP 443 from 0.0.0.0/0
  - TCP 22 from `<YOUR_PUBLIC_IP>/32`
- 그 외 deny(기본)

참고:
- OCI security rules: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm

---

## 4. OS 방화벽(iptables) 기본

주의:
- OCI Ubuntu 이미지는 UFW 사용이 권장되지 않는 케이스가 있어 iptables 기반으로 접근한다.

참고:
- compute best practices: https://docs.oracle.com/en-us/iaas/Content/Compute/References/bestpracticescompute.htm
- Ubuntu OCI 트래픽 허용(iptables): https://blogs.oracle.com/developers/enabling-network-traffic-to-ubuntu-images-in-oracle-cloud-infrastructure

---

## 5. 초기 설치(SSH 접속 후)

### 5.1 기본 업데이트
```bash
sudo apt-get update -y
sudo apt-get upgrade -y
```

### 5.2 Docker 설치(권장: 공식 가이드 기반)
- docker engine 설치 후 `docker compose` 사용 가능 상태로 만든다.

---

## 6. 운영 시크릿 파일(표준)

```bash
sudo mkdir -p /etc/everbit
sudo nano /etc/everbit/everbit.env
sudo chmod 600 /etc/everbit/everbit.env
```

- 내용은 `docs/operations/environments.md`의 Server/DB/Push 항목을 참고해 채운다.
- repo 폴더 내부에 `.env`를 두지 않는다.

---

## 7. 도메인 연결 준비

- VM Public IP 확인
- DNS에 `api.everbit.kr` A 레코드로 Public IP 등록
- 프론트(Vercel)에서 서브도메인을 외부로 포인팅 가능

참고:
- Vercel 서브도메인 외부 포인팅: https://vercel.com/kb/guide/pointing-subdomains-to-external-services

---

## 8. 완료 조건(Done)

- [ ] 80/443만 외부에서 접근 가능
- [ ] 22는 내 IP에서만 접근 가능
- [ ] `/etc/everbit/everbit.env` 생성 + 600 권한
- [ ] Docker 설치 + compose 동작
- [ ] `api.everbit.kr`가 VM을 가리킴
