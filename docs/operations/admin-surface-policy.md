# Admin Surface 정책 (Grafana / Prometheus / Jenkins)

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- 관리 UI/모니터링 UI를 인터넷에 직접 노출하지 않는다.
- 기본 접근은 SSH 터널로 고정한다(ADR-0006).

---

## 1. 포트/노출 정책(강제)

### 1.1 외부 공개 허용
- 80/tcp: HTTP(LE 챌린지 + 443 리다이렉트)
- 443/tcp: HTTPS(API)
- 22/tcp: SSH(본인 IP /32 제한, 키 인증)

### 1.2 절대 외부 공개 금지
- 9090(Prometheus), 3000(Grafana), Jenkins 포트, 5432/6379/9092 등

---

## 2. 기본 접근(표준): SSH 터널

전제:
- 운영 compose(prod)에서 Admin 서비스 포트는 **127.0.0.1에만 publish**한다.

Mac 예시:
```bash
# Grafana
ssh -i ~/.ssh/<YOUR_SSH_KEY>   -L 3000:127.0.0.1:3000   ubuntu@api.everbit.kr

# Jenkins
ssh -i ~/.ssh/<YOUR_SSH_KEY>   -L 8081:127.0.0.1:8081   ubuntu@api.everbit.kr

# Prometheus
ssh -i ~/.ssh/<YOUR_SSH_KEY>   -L 9090:127.0.0.1:9090   ubuntu@api.everbit.kr
```

---

## 3. 예외 공개(Break-glass, 제한적)

원칙적으로 금지한다. 필요 시 아래를 모두 만족해야 한다.
- 서브도메인 + TLS
- IP allowlist(최소)
- 강한 인증(Basic/SSO)
- 서비스 포트는 열지 않고, nginx 443만 통해서 프록시

레퍼런스:
- Grafana reverse proxy: https://grafana.com/tutorials/run-grafana-behind-a-proxy/
- Jenkins reverse proxy: https://www.jenkins.io/doc/book/system-administration/reverse-proxy-configuration-with-jenkins/

---

## 4. 운영 체크리스트

- [ ] docker compose(prod)에서 Admin 포트가 127.0.0.1에만 bind 되었는지 확인
- [ ] OCI NSG에 Admin 포트가 열려 있지 않은지 확인
- [ ] Break-glass를 했다면 종료 후 즉시 원복 + 로그 점검
