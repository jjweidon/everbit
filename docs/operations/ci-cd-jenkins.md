# CI/CD (Jenkins)

Status: **Ready for Execution (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- v2 운영 서버 배포를 “재현 가능 + 롤백 가능 + 노출 최소화” 형태로 고정한다.
- 배포 과정에서 시크릿이 Jenkins에 유입되지 않게 한다.
- 실거래 시스템 특성상 “자동 배포”보다 “안전한 반자동”을 기본값으로 둔다.

관련 문서(SoT):
- `docs/operations/admin-surface-policy.md` (Jenkins 노출 금지/SSH 터널)
- `docs/operations/deploy.md` (운영 배포 표준)
- `docs/operations/environments.md` (시크릿 주입 표준)

---

## 1. 원칙(강제)

1) Jenkins는 **외부에 직접 노출하지 않는다**
- 접근은 SSH 터널이 표준이다.
- 포트 publish는 127.0.0.1 바인딩만 허용한다.

2) 런타임 시크릿은 Jenkins가 관리하지 않는다
- 운영 시크릿은 `/etc/everbit/everbit.env`(600) 또는 VM 환경변수로만 주입한다.
- Jenkins Credentials에는 “배포를 위한 최소 정보”만 저장한다(SSH 키, 레지스트리 토큰 등).

3) 배포는 기본적으로 “수동 승인(Manual)” 단계를 포함한다
- 실거래 시스템에서 auto-deploy는 장애 확산 속도를 높인다.

---

## 2. 배포 대상/흐름 (권장 기본)

### 2.1 프론트엔드
- Vercel의 Git 연동을 기본으로 한다.
- Jenkins는 프론트 배포를 책임지지 않는다(v2 MVP).

### 2.2 백엔드/인프라(단일 VM)
권장 흐름:
1) Jenkins: server 테스트/빌드
2) Jenkins: Docker 이미지 빌드 & 레지스트리 푸시(예: GHCR)
3) Jenkins: (수동 승인) 운영 VM에 SSH 접속 → compose pull/up
4) 배포 후 헬스 체크 및 로그 확인

---

## 3. Jenkins 배치(운영 안정성 우선)

### 3.1 실행 위치
- 운영 VM(OCI 단일 VM) 내부에서 Docker 컨테이너로 실행한다.
- Jenkins 데이터는 영속 볼륨으로 유지한다.

### 3.2 노출/포트 정책(강제)
- Jenkins UI 포트는 외부 공개 금지
- publish 예시(권장): `127.0.0.1:8081 -> jenkins:8080`

접근 방식:
- `docs/operations/admin-surface-policy.md`의 SSH 터널 예시를 따른다.

---

## 4. Jenkins Credentials (최소 구성)

필수(권장):
- GHCR push 권한 계정/토큰(최소 권한)
- 운영 VM 접속용 SSH 키(키 인증)

금지:
- `KAKAO_CLIENT_SECRET`, `JWT_*_SECRET`, `UPBIT_KEY_MASTER_KEY`, `VAPID_PRIVATE_KEY` 등 **런타임 시크릿**

---

## 5. Pipeline 설계 (Jenkinsfile 권장)

### 5.1 태깅 규칙(권장)
- 이미지 태그: Git SHA 기반
- 롤백은 이전 sha로 재배포하면 된다.

### 5.2 단계(권장)
Stage A — Verify
- server: `./gradlew test`

Stage B — Build Image
- `docker build`

Stage C — Push Image
- registry login 후 push

Stage D — Deploy (Manual)
- 운영 VM에 SSH로 접속하여 compose pull/up
- 배포 후 헬스 체크

### 5.3 Jenkinsfile 스켈레톤(예시)
```groovy
pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'DEPLOY_PROD', defaultValue: false, description: '운영 배포 수행')
  }

  environment {
    REGISTRY = "ghcr.io/<owner>"
    IMAGE    = "${REGISTRY}/everbit-server:${env.GIT_COMMIT}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Test') {
      steps {
        dir('server') {
          sh './gradlew test'
        }
      }
    }

    stage('Build Image') {
      steps {
        sh "docker build -t ${IMAGE} -f server/Dockerfile server"
      }
    }

    stage('Push Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'ghcr', usernameVariable: 'U', passwordVariable: 'P')]) {
          sh 'echo $P | docker login ghcr.io -u $U --password-stdin'
          sh "docker push ${IMAGE}"
        }
      }
    }

    stage('Deploy (Manual)') {
      when { expression { return params.DEPLOY_PROD } }
      steps {
        input message: "운영 배포를 진행합니다. Kill Switch OFF 상태인지 확인했습니까?"
        withCredentials([sshUserPrivateKey(credentialsId: 'prod-ssh', keyFileVariable: 'KEY', usernameVariable: 'USER')]) {
          sh '''
            ssh -i $KEY -o StrictHostKeyChecking=no $USER@api.everbit.kr \
              'cd /opt/everbit && docker compose -f docker/compose.yaml -f docker/compose.prod.yaml pull && docker compose -f docker/compose.yaml -f docker/compose.prod.yaml up -d'
          '''
        }
      }
    }
  }
}
```

---

## 6. 운영 체크리스트 (배포 전/후)

배포 전:
- [ ] Kill Switch 상태 확인(배포 중 매매 중단 권장)
- [ ] 운영 VM 디스크 여유 확인

배포 후:
- [ ] 서버 health 정상
- [ ] 로그인/키 복호화/대시보드 핵심 플로우 smoke test

---

## 7. 롤백 규칙(강제)

- “이전 이미지 태그(sha)”로 재배포가 롤백의 표준이다.
- DB 마이그레이션이 포함된 배포는 롤백 비용이 급증한다.
  - v2 MVP에서는 파괴적 마이그레이션을 피하고 확장적 변경을 우선한다.
