# Nginx + TLS(HTTPS) 세팅 (Let's Encrypt)

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- `api.everbit.kr`에 대해 HTTPS를 구성한다.
- 외부 노출 포트는 80/443만 유지한다.

---

## 1. 전제
- DNS `api.everbit.kr` A 레코드가 VM Public IP를 가리킨다.
- OCI NSG/iptables에서 80/443 허용.
- 운영에서는 nginx만 외부로 publish한다.

---

## 2. Nginx Reverse Proxy 기본

권장 정책:
- HTTP(80) → HTTPS(443) 리다이렉트
- `/api` → Spring(내부 8080)
- `/actuator/**`는 외부 차단(또는 allowlist)

---

## 3. Let's Encrypt 발급(HTTP-01)

운영에서는 certbot 컨테이너를 사용한다.

예시(웹루트 방식):
```bash
# 1) 디렉터리/볼륨 준비(compose에 정의되어 있다고 가정)
# letsencrypt: /etc/letsencrypt
# certbot-www: /var/www/certbot

# 2) 인증서 발급
docker run --rm   -v letsencrypt:/etc/letsencrypt   -v certbot-www:/var/www/certbot   certbot/certbot certonly   --webroot -w /var/www/certbot   -d api.everbit.kr   --email admin@everbit.kr --agree-tos --no-eff-email
```

발급 후 nginx reload:
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml exec nginx nginx -s reload
```

---

## 4. 자동 갱신(renew)

cron 또는 systemd timer로 1일 1회 실행:
```bash
docker run --rm   -v letsencrypt:/etc/letsencrypt   -v certbot-www:/var/www/certbot   certbot/certbot renew --webroot -w /var/www/certbot

docker compose -f docker/compose.yaml -f docker/compose.prod.yaml exec nginx nginx -s reload
```

---

## 5. 완료 조건(Done)

- [ ] https://api.everbit.kr 접속 시 인증서 정상
- [ ] http://api.everbit.kr 접속 시 https로 리다이렉트
- [ ] `/actuator/**` 외부 차단(또는 allowlist)
