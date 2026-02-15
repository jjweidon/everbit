# Admin Surface 정책 (Grafana / Prometheus / Jenkins)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

단일 VM 올인원 구성에서 “관리 UI/모니터링 UI”를 인터넷에 직접 노출하면 공격면이 급증한다.  
따라서 Everbit v2는 아래를 원칙으로 한다.

- **기본값: Admin Surface는 외부 비공개**
- “필요할 때만”, “최소 기간만”, “최소 노출로” 접근 경로를 연다.

---

## 2. 용어

- Admin Surface: Grafana UI, Prometheus UI, Jenkins UI, (옵션) Kafka UI, DB Admin UI 등
- Public Surface: `api.everbit.kr` API(필수 공개)

---

## 3. 포트/노출 정책(고정)

### 3.1 공개 허용
- 80/tcp: HTTP(LE 챌린지 + 443 리다이렉트)
- 443/tcp: HTTPS(API)
- 22/tcp: SSH (Source IP 제한 필수)

### 3.2 절대 공개 금지(운영 규칙)
- 9090(Prometheus)
- 3000(Grafana)
- Jenkins 포트(예: 8081)
- 5432(Postgres), 6379(Redis), 9092(Kafka)

정책 근거:
- 위 서비스들은 “인터넷 공개”를 전제로 설계되지 않았고, 플러그인/의존성 취약점으로 피해 범위가 커진다.

---

## 4. 접근 방식 표준

### 4.1 기본 접근(권장): SSH 터널
가장 단순하고 안전하다.

전제:
- docker compose(prod)에서 Admin 서비스 포트는 **127.0.0.1에만 바인딩**하거나, 아예 publish 하지 않는다.

Mac에서:

```bash
# Grafana (로컬 3000 → 서버 127.0.0.1:3000)
ssh -i ~/.ssh/<YOUR_SSH_KEY> \
  -L 3000:127.0.0.1:3000 \
  ubuntu@api.everbit.kr

# Jenkins (로컬 8081 → 서버 127.0.0.1:8081)
ssh -i ~/.ssh/<YOUR_SSH_KEY> \
  -L 8081:127.0.0.1:8081 \
  ubuntu@api.everbit.kr
```

- Grafana: http://localhost:3000
- Jenkins: http://localhost:8081

### 4.2 예외 접근(선택): Nginx 뒤로 “제한된 공개”
원칙적으로 금지하지만, 아래 조건을 모두 만족하면 허용한다.

필수 조건(모두):
- 별도 서브도메인 사용: `grafana.everbit.kr`, `jenkins.everbit.kr`
- TLS 필수
- IP allowlist(최소) + 강한 인증(기본 인증/SSO 등)
- 서비스 자체 포트는 외부로 열지 않고, **Nginx 443만 열어서 프록시**

---

## 5. Grafana 운영 스펙

### 5.1 Reverse Proxy / Sub-path 설정
Grafana는 reverse proxy를 통해 sub-path로 서비스할 경우 `root_url`과 `serve_from_sub_path`를 적절히 설정해야 한다.

레퍼런스:
- Grafana “Run behind a reverse proxy”: https://grafana.com/tutorials/run-grafana-behind-a-proxy/
- Grafana config(`root_url`, `serve_from_sub_path`): https://grafana.com/docs/grafana/latest/setup-grafana/configure-grafana/

운영 정책:
- 가능하면 **서브도메인 방식(grafana.everbit.kr)**을 사용(구성이 단순)
- sub-path(`/grafana`)는 최후의 수단(추가 설정/리다이렉트 이슈 가능)

### 5.2 보안 설정
Grafana는 reverse proxy로 인증을 위임할 수도 있고(Auth Proxy), 자체 인증을 쓸 수도 있다.

레퍼런스:
- Grafana Configure security: https://grafana.com/docs/grafana/latest/setup-grafana/configure-security/
- Grafana Auth Proxy: https://grafana.com/docs/grafana/latest/setup-grafana/configure-access/configure-authentication/auth-proxy/

운영 정책(최소):
- anonymous access 금지
- 관리자 비밀번호는 초기 설치 직후 변경
- 외부 공개가 필요하면 **Auth Proxy + allowlist** 조합을 우선 검토

---

## 6. Jenkins 운영 스펙

### 6.1 Reverse Proxy
Jenkins는 reverse proxy 구성 예시를 공식 문서로 제공한다.

레퍼런스:
- Jenkins reverse proxy overview: https://www.jenkins.io/doc/book/system-administration/reverse-proxy-configuration-with-jenkins/
- Jenkins reverse proxy (Nginx): https://www.jenkins.io/doc/book/system-administration/reverse-proxy-configuration-with-jenkins/reverse-proxy-configuration-nginx/

### 6.2 접근 제어
Jenkins는 reverse proxy(Nginx/Apache)에서 접근을 제한하는 방식을 문서로 언급한다.
즉, “Jenkins 자체에 닿기 전에” 네트워크 레벨에서 차단하는 게 의미가 있다.

레퍼런스:
- Jenkins Access Control: https://www.jenkins.io/doc/book/security/access-control/

운영 정책(고정):
- Jenkins는 외부 비공개가 기본
- 외부 공개가 불가피한 경우에도:
  - 443(TLS) 뒤로만 노출
  - IP allowlist + 인증(기본 인증/SSO)
  - 플러그인 최소화 + 정기 업데이트 + credential 최소권한

---

## 7. Prometheus 운영 스펙

- Prometheus UI(9090)는 외부 노출 금지
- Grafana만 접근 가능(내부 네트워크)
- 필요 시 SSH 터널로만 접근

---

## 8. Break-glass 절차(임시 공개)

임시 공개는 “사고 대응” 용도로만 허용한다.

절차:
1) 노출 필요 사유/기간을 기록(runbook에 남김)
2) IP allowlist 추가(최대 1~2개)
3) 작업 종료 즉시 allowlist 제거 + 접근 로그 확인
4) Jenkins/Grafana 비밀번호/토큰 회전(필요 시)

---

## 9. 다음 문서
- `docs/operations/deploy.md`
- `docs/operations/runbook.md`
