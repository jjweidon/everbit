# OCI 세팅 (Oracle Cloud Free Tier, Day-0) — v3

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

목표:
- OCI Always Free **E2.1.Micro(1GB)** 단일 VM에 everbit v2 API(Spring Boot) + 리버스 프록시만 둔다.
- **DB/Auth/Storage는 Supabase**로 분리하며, VM에는 DB 컨테이너를 띄우지 않는다.
- 과금 리스크를 최소화하고, 네트워크/포트 정책을 Day-0에서 고정한다.

운영 아키텍처(SoT):
- **Frontend(Next.js)**: Vercel
- **Backend(Spring Boot API)**: OCI E2.1.Micro
- **DB/Auth/Storage**: Supabase

---

## 1. 가입 전 체크(실패 방지)

- VPN/프록시 OFF
- 브라우저 확장(광고차단/추적차단)으로 가입 폼이 깨지면 비활성화
- 결제수단은 **신용카드 또는 신용카드처럼 동작하는 체크카드** 권장
- 1인 1계정 원칙 준수

참고:
- Free tier FAQ: https://www.oracle.com/cloud/free/faq/
- 가입 문서: https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/signingup_topic-Sign_Up_for_Free_Oracle_Cloud_Promotion.htm

---

## 2. 인스턴스 생성(Always Free — E2.1.Micro)

권장:
- **Shape**: `VM.Standard.E2.1.Micro` (Always Free, 1GB RAM)
- **OS**: Ubuntu LTS

주의:
- E2.1.Micro는 1GB 메모리만 제공하므로 **VM 위에 DB(Postgres/Redis) 컨테이너를 띄우지 않는다.** DB/Auth/Storage는 Supabase를 사용한다.

---

## 3. Swap 설정(필수, OOM 방지)

1GB VM에서 Spring Boot 등이 OOM을 일으키지 않도록 **2GB swap**을 반드시 설정한다.

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

---

## 4. 네트워크(VCN + NSG + Security List) — 순서 준수

원칙:
- **외부 공개는 80/443만**
- **SSH(22)는 내 IP /32만**

**반드시 아래 순서를 지켜라.** (NSG를 먼저 붙인 뒤, Subnet Security List의 광범위 룰을 제거해 락아웃을 방지한다.)

1) **NSG(Security Rules) 생성 및 규칙 정의**
   - Ingress allow:
     - TCP 80 from 0.0.0.0/0
     - TCP 443 from 0.0.0.0/0
     - TCP 22 from `<YOUR_PUBLIC_IP>/32`
   - 그 외 deny(기본)

2) **NSG를 VNIC에 Attach**
   - 인스턴스의 Primary VNIC에 위 NSG를 연결한다.

3) **Subnet Security List 정리**
   - NSG가 적용된 후, Subnet의 Security List에서 **0.0.0.0/0에 대한 불필요한 인바운드 룰**을 제거하거나 축소한다.
   - 최종적으로 외부에서 열리는 포트는 80, 443, 22(본인 IP만)만 유지한다.

참고:
- OCI security rules: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm

---

## 5. OS 방화벽(iptables) 기본

주의:
- OCI Ubuntu 이미지는 UFW 사용이 권장되지 않는 케이스가 있어 iptables 기반으로 접근한다.

참고:
- compute best practices: https://docs.oracle.com/en-us/iaas/Content/Compute/References/bestpracticescompute.htm
- Ubuntu OCI 트래픽 허용(iptables): https://blogs.oracle.com/developers/enabling-network-traffic-to-ubuntu-images-in-oracle-cloud-infrastructure

---

## 6. 초기 설치(SSH 접속 후)

### 6.1 기본 업데이트
```bash
sudo apt-get update -y
sudo apt-get upgrade -y
```

### 6.2 Docker 설치(권장: 공식 가이드 기반)
- Docker Engine 설치 후 `docker compose` 사용 가능 상태로 만든다.

### 6.3 Spring Boot JVM 메모리 상한(필수)

E2.1.Micro(1GB)에서 OOM을 막기 위해 **JVM 힙 상한을 강제**한다.

- **방법 A — Docker Compose**  
  backend 서비스에 환경변수:
  ```yaml
  environment:
    - JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m -XX:MaxMetaspaceSize=96m
  ```
- **방법 B — systemd / 직접 실행**  
  `JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m -XX:MaxMetaspaceSize=96m` 를 서비스 환경에 설정한다.

운영 시크릿은 repo 내부 `.env`가 아니라 **`/etc/everbit/everbit.env`**에서만 로드한다.

---

## 7. 운영 시크릿 파일(표준)

```bash
sudo mkdir -p /etc/everbit
sudo nano /etc/everbit/everbit.env
sudo chmod 600 /etc/everbit/everbit.env
```

- 내용은 `docs/operations/environments.md`의 Server/DB(Supabase)/Push 항목을 참고해 채운다.
- **repo 폴더 내부에 `.env`를 두지 않는다.**

---

## 8. 도메인 연결 준비

- VM Public IP 확인
- DNS에 `api.everbit.kr` A 레코드로 Public IP 등록
- 프론트(Vercel)에서 서브도메인을 외부로 포인팅 가능

참고:
- Vercel 서브도메인 외부 포인팅: https://vercel.com/kb/guide/pointing-subdomains-to-external-services

---

## 9. 완료 조건(Done)

- [ ] Shape: E2.1.Micro, Swap 2GB 적용, swappiness=10
- [ ] 80/443만 외부에서 접근 가능
- [ ] 22는 내 IP에서만 접근 가능
- [ ] `/etc/everbit/everbit.env` 생성 + 600 권한
- [ ] Docker 설치 + compose 동작
- [ ] Spring Boot 실행 시 JVM 메모리 상한(-Xms128m -Xmx256m) 적용
- [ ] VM에 DB 컨테이너 없음(DB/Auth/Storage는 Supabase)
- [ ] `api.everbit.kr`가 VM을 가리킴
