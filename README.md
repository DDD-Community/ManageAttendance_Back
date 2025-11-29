# ManageAttendance

운영진 출석 관리 애플리케이션 백엔드 서버

## 기술 스택

### 개발 환경
- **Java**: 21
- **빌드 도구**: Gradle
- **Spring Boot**: 3.5.9-SNAPSHOT

### 데이터베이스
- **PostgreSQL**: 메인 데이터베이스

### 테스팅
- **Spring Boot Starter Test**: 단위 및 통합 테스트
- **Spring Security Test**: 보안 테스트
- **Spring REST Docs MockMVC**: API 문서 생성
- **JUnit Platform Launcher**: 테스트 실행

## 시작하기

### 사전 요구사항
- Java 21 이상
- PostgreSQL 데이터베이스

### 빌드 및 실행

#### Gradle을 사용한 빌드
```bash
./gradlew build
```

#### 애플리케이션 실행
```bash
./gradlew bootRun
```

### 테스트 실행
```bash
./gradlew test
```
