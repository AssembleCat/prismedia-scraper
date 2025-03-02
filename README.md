# Prismedia Scraper (프리즈미디어 스크래퍼)

## ⚠️ Deprecated (폐기된 기능)
### 직접 스크래핑 방법
> **2025년 3월 2일부로 직접 스크래핑 방식은 더 이상 사용되지 않습니다.**  

## 프로젝트 소개
Prismedia Scraper는 다양한 언론사의 RSS 피드를 수집하고 저장하는 자동화된 뉴스 스크래핑 시스템입니다. 매시간 정각에 실행되어 최신 뉴스 기사를 수집하고 저장합니다.

## 주요 기능
- 다양한 언론사의 RSS 피드 자동 수집
- 언론사별 맞춤형 스크래핑 전략 적용
- 중복 기사 방지를 위한 자동 필터링
- 시간별 자동 수집 스케줄링

## 🛠 기술 스택
| 카테고리 | 기술 |
|---------|------|
| 언어 | Kotlin |
| 프레임워크 | Spring Boot |
| 컨테이너화 | Docker |
| 빌드 도구 | Gradle |
| RSS 파싱 | Rome Tools |

### 요구사항
- JDK 21
- Docker
- Docker Compose

### 설치 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# Docker 컨테이너 실행
docker-compose up -d --build
```

### 프로젝트 구조
```
prismedia-scraper/
├── 📂 src/main/kotlin/
│   └── org/prismedia/scraper/
│       ├── service/        # 스크래핑 서비스
│       ├── repository/     # 데이터 저장소
│       ├── entity/         # 데이터 모델
│       └── factory/        # 스크래핑 전략
├── 📄 docker-compose.yml   # Docker 설정
└── 📄 build.gradle.kts     # 빌드 설정
```

### 설정 파일
| 파일 | 설명 |
|------|------|
| `src/main/resources/rss/rss.csv` | RSS 피드 소스 목록 |
| `application.yml` | 애플리케이션 설정 |
| `docker-compose.yml` | Docker 환경 설정 |