# Client(Vercel) 배포

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-11 (Asia/Seoul)

목표:
- Client(Next.js)를 Vercel에 배포할 수 있도록 **Git push 자동 배포**와 **터미널 수동 배포**를 모두 지원한다.
- 루트 레포지토리가 현재 public이며, 추후 **private 전환** 시에도 동일한 방식으로 운영할 수 있도록 정리한다.

관련 문서:
- `docs/operations/environments.md` (Client 환경변수)
- `docs/operations/deploy.md` (백엔드/VM 배포 표준)
- `docs/operations/ci-cd-jenkins.md` (Jenkins는 프론트 배포를 책임지지 않음)

---

## 1. 전제

- **배포 대상**: `client/` 패키지(Next.js)
- **호스팅**: Vercel
- **레포지토리**: 단일 루트(모노레포). Vercel 프로젝트의 **Root Directory**는 `client`로 설정한다.

---

## 2. Vercel 프로젝트 설정(최초 1회)

### 2.1 GitHub 연동

1. [Vercel](https://vercel.com) 로그인 후 **Add New Project**.
2. **Import Git Repository**에서 해당 GitHub 저장소 선택.
3. **Configure Project** 단계에서:
   - **Root Directory**: `client` 로 설정(필수).  
     → "Edit" 클릭 후 `client` 입력하고 "Include subdirectory" 적용.
   - **Framework Preset**: Next.js (자동 감지됨).
   - **Build Command**: `pnpm run build` (또는 기본값 유지).
   - **Output Directory**: 비워 둠(Next.js는 Vercel이 자동 처리).
4. **Environment Variables**에 운영용 변수 설정(예: `NEXT_PUBLIC_API_BASE_URL`, `NEXT_PUBLIC_VAPID_PUBLIC_KEY` 등).  
   → SoT: `docs/operations/environments.md` § 3.7.
5. **Deploy** 실행.

이후 **지정한 브랜치**(예: `v2`)에 push가 되면 해당 브랜치 기준으로 자동 빌드·배포된다.

### 2.2 레포지토리를 Private으로 전환할 때

- 루트 레포지토리가 현재 **public**이어도, 추후 **private**으로 전환해도 배포 방식은 동일하다.
- 전환 후 필요한 작업:
  1. Vercel 대시보드 → **Project → Settings → Git**.
  2. GitHub 연동이 끊어졌다면 **Reconnect** 또는 권한 재승인.
  3. Vercel GitHub App(또는 OAuth)이 해당 **private** 저장소에 대한 접근 권한을 갖도록 GitHub에서 승인.

코드/설정 변경은 필요 없고, **저장소 접근 권한만 유지**하면 push 시 자동 배포가 계속 동작한다.

---

## 3. 자동 배포(Git push)

- **동작**: 설정한 브랜치(예: `v2`)에 push 시 Vercel이 해당 브랜치를 빌드하고 프로덕션(또는 프리뷰)에 배포한다.
- **Root Directory**가 `client`로 되어 있어야 `client/` 기준으로 빌드된다.
- 배포 결과는 Vercel 대시보드에서 확인한다.

---

## 4. 수동 배포(터미널)

Git 없이 로컬에서 바로 배포하고 싶을 때 사용한다.

### 4.1 사전 조건

- Vercel CLI가 사용 가능해야 한다.  
  - **방법 A**: `npx vercel` 사용(권장). `client`에 vercel 패키지를 넣지 않으며, 배포 시 `npx`로 CLI를 실행한다.  
  - **방법 B**: 전역 설치: `npm i -g vercel`.
- 최초 1회: `client` 디렉터리에서 `vercel login`(또는 `npx vercel login`)으로 로그인.  
- 최초 1회(프로젝트 연결): `client`에서 `vercel link`로 Vercel 팀/프로젝트와 연결.  
  - 모노레포이므로 **프로젝트의 Root Directory가 이미 `client`로 설정된 경우**, 루트가 아닌 **`client` 디렉터리에서** `vercel link` 및 배포 명령을 실행한다.

### 4.2 명령 실행 위치

- **반드시 `client` 디렉터리**에서 실행한다.

```bash
cd client
```

### 4.3 프리뷰 배포(Preview)

- 현재 로컬/브랜치 기준으로 프리뷰 URL에 배포한다.

```bash
pnpm run deploy:preview
# 또는
pnpm exec vercel
```

- 최초 실행 시 프로젝트 연결 등 질의에 응답한다. 이후에는 빌드 후 프리뷰 URL이 출력된다.

### 4.4 프로덕션 배포(Production)

- 프로덕션 도메인(예: everbit.kr)에 배포한다.

```bash
pnpm run deploy
# 또는
pnpm exec vercel --prod
```

- 실거래 연동 클라이언트이므로, 배포 전에 빌드/스모크 테스트를 권장한다.

### 4.5 루트에서 실행하고 싶을 때

- 루트에서 실행하려면 반드시 **작업 디렉터리를 `client`로 지정**해야 한다.

```bash
pnpm --filter everbit-client run deploy:preview
pnpm --filter everbit-client run deploy
```

또는:

```bash
pnpm -C client exec vercel --prod
```

---

## 5. 정리

| 방식           | 트리거           | 실행 위치   | 비고                          |
|----------------|------------------|------------|-------------------------------|
| 자동 배포      | Git push to v2   | Vercel     | Root Directory = `client` 필수 |
| 수동(프리뷰)   | 터미널           | `client/`  | `pnpm run deploy:preview`     |
| 수동(프로덕션) | 터미널           | `client/`  | `pnpm run deploy`             |

- 레포지토리를 **private**으로 전환해도, GitHub 연동 권한만 유지하면 자동/수동 배포 모두 그대로 사용 가능하다.
