# 시크릿 관리

## Git 커밋 금지

다음 항목은 **절대** Git에 커밋하지 않는다:

* `.env.local`, `.env.prod`, 운영 환경변수 dump
* 업비트 access/secret key
* JWT secret
* DB password

## 운영 주입

* VM 환경변수(또는 docker compose `.env` 파일을 "서버 로컬에만" 보관)
* `.env*` 파일은 `.gitignore` 및 `.cursorignore`에 등록

## 키 암호화

* 업비트 키 암호화 키(MASTER_KEY)는 **운영 VM 환경변수**로만 주입
* DB에 저장되는 업비트 키는 AES-GCM 등 인증 암호로 암호화

## 체크리스트

- [ ] `.env.local` 등 로컬 시크릿이 Git에 커밋되지 않음
- [ ] 업비트 키가 평문으로 저장되지 않음
- [ ] JWT secret이 코드/설정 파일에 하드코딩되지 않음
- [ ] `docs/security/secrets.md`가 팀에 공유됨
- [ ] `.cursorignore`에 시크릿 관련 경로 등록됨
