# 답변 규칙

- 모든 답변은 **한국어**로 작성되어야 합니다.
- API 문서 및 외부 연동 레퍼런스는 **영어**로 유지할 수 있습니다.
- 코드 자동화 시 Swagger/OpenAPI 명세 기반 생성을 우선시합니다.

## 빌드/테스트 명령어

- 빌드: `./gradlew build`
- 테스트: `./gradlew test`
- 코드 포맷팅: `./gradlew spotlessApply`

## 코드 스타일

- 줄 길이: 100자 이하 권장
- 클래스/인터페이스: PascalCase
- 변수/함수: camelCase
- 상수: UPPER_SNAKE_CASE
- 패키지 구조: `com.ddd.manage_attendance.domain.{domain}.{layer}`
- 메서드 길이: 30줄 이하 권장
- 중첩 depth: 최대 2단계
- 주석: JavaDoc 또는 inline 주석은 필요한 경우에만 작성

### Spotless 코드 포맷터 적용 기준

본 프로젝트는 [Spotless](https://github.com/diffplug/spotless)를 이용하여 Java 코드 스타일을 자동화합니다.

- **Formatter**: Google Java Format (AOSP 스타일)
- **불필요한 import 제거**: 자동 수행
- **줄 끝 공백 제거**: 자동 적용
- **파일 마지막 줄 개행 보장**: 자동 적용

코드를 커밋하기 전에 `./gradlew spotlessApply`를 실행해주세요.

## 프로젝트 구조

본 프로젝트는 **레이어드 아키텍처 + DDD 패턴 일부 적용**을 따릅니다.

### 패키지 구조

```
com.ddd.manage_attendance
├── core/                    # 공통 설정 및 유틸리티
│   ├── common/              # 공통 엔티티, DTO, 유틸리티
│   ├── config/              # Spring 설정 클래스
│   └── exception/           # 공통 예외 클래스
└── domain/                   # 도메인별 패키지
    └── {domain}/            # 도메인명 (예: auth, sample, oauth)
        ├── api/              # REST API 계층
        │   └── dto/          # Request/Response DTO
        ├── application/      # Application Service 계층
        ├── domain/           # Domain 계층 (Entity, Service, Repository)
        └── infrastructure/   # Infrastructure 계층 (외부 연동)
```

### 계층별 책임

#### `api` (Controller 계층)
- REST API 진입점
- 요청/응답 DTO 변환
- Swagger/OpenAPI 어노테이션 관리
- 입력 검증 (`@Valid`)

**예시:**
```java
@RestController
@RequestMapping("/api/{domain}")
@RequiredArgsConstructor
@Tag(name = "{도메인} API")
public class {Domain}Controller {
    private final {Domain}Service {domain}Service;
    
    @PostMapping
    @Operation(summary = "생성", description = "...")
    public {Domain}Response create(@RequestBody @Valid {Domain}SaveRequest request) {
        return {domain}Service.create(request);
    }
}
```

#### 📂 `api.dto` (DTO 계층)
- **Request DTO**: `record` 사용 권장
- **Response DTO**: `record` 사용 권장
- Validation 어노테이션 적용
- Swagger 스키마 어노테이션 적용

**예시:**
```java
// Request DTO
public record {Domain}SaveRequest(
    @NotBlank(message = "...")
    @Size(max = 64, message = "...")
    @Schema(description = "...", example = "...")
    String field
) {
    public {Domain} toEntity() {
        return {Domain}.create(field());
    }
}

// Response DTO
public record {Domain}Response(
    @Schema(description = "...", example = "...")
    Long id,
    String field
) {
    public static {Domain}Response from({Domain} domain) {
        return new {Domain}Response(domain.getId(), domain.getField());
    }
}
```

#### 📂 `application` (Application Service 계층)
- 유스케이스 조합 및 트랜잭션 관리
- 도메인 서비스 호출
- DTO ↔ Domain 변환

**예시:**
```java
@Service
@RequiredArgsConstructor
public class {Domain}Service {
    private final {Domain}Repository {domain}Repository;
    
    @Transactional
    public {Domain}Response create({Domain}SaveRequest request) {
        {Domain} domain = request.toEntity();
        {Domain} saved = {domain}Repository.save(domain);
        return {Domain}Response.from(saved);
    }
}
```

#### 📂 `domain` (Domain 계층)
- **Entity**: JPA 엔티티, `BaseEntity` 상속
- **Domain Service**: 핵심 비즈니스 로직
- **Repository**: Spring Data JPA 인터페이스
- **정적 팩토리 메서드**: `Entity.from()` 또는 `Entity.create()` 패턴 사용

**예시:**
```java
@Entity
@Table(name = "{table_name}")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class {Domain} extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 정적 팩토리 메서드
    public static {Domain} create(String field) {
        {Domain} domain = new {Domain}();
        domain.field = field;
        return domain;
    }
    
    // 비즈니스 메서드
    public void modify(String field) {
        this.field = field;
    }
}
```

#### 📂 `infrastructure` (Infrastructure 계층)
- 외부 시스템 연동 (OAuth, 외부 API 등)
- 기술적 구현 세부사항
- Properties 클래스 (`@ConfigurationProperties`)

## 코딩 원칙

### DTO 작성 규칙
- **DTO는 `record`로 작성** (Java 14+)
- Request DTO는 `toEntity()` 메서드로 Entity 변환 가능하도록 구현
- Response DTO는 `from(Entity)` 정적 팩토리 메서드로 생성
- Validation 어노테이션은 record 파라미터에 직접 적용

### Entity 작성 규칙
- `BaseEntity` 상속하여 공통 필드 활용 (`createdDate`, `createdId`, `updatedDate`, `updatedId`)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- 정적 팩토리 메서드로 생성 (`create()`, `registerUser()` 등)
- 비즈니스 로직은 Entity 내부 메서드로 구현
- Builder 패턴은 필요시에만 사용 (복잡한 생성 로직)

### Service 작성 규칙
- `@Transactional`은 Service 계층에서만 사용
- 읽기 전용 작업은 `@Transactional(readOnly = true)` 사용
- 도메인 로직은 Domain Service 또는 Entity 메서드로 위임
- Application Service는 조합과 흐름 제어에 집중

### Repository 작성 규칙
- Spring Data JPA 인터페이스 사용
- 복잡한 쿼리는 `@Query` 어노테이션 사용
- `@QueryHints`로 쿼리 힌트 추가 가능
- 메서드명은 `findBy`, `existsBy`, `countBy` 등 Spring Data JPA 규칙 따름

### 예외 처리 규칙
- 도메인별 Custom Exception은 `BaseException` 상속
- 예외 메시지는 한국어로 작성
- 예외는 가능한 한 구체적으로 작성

**예시:**
```java
public class {Domain}NotFoundException extends BaseException {
    public {Domain}NotFoundException(Long id) {
        super(String.format("%s를 찾을 수 없습니다. ID: %d", "{도메인명}", id));
    }
}
```

## 공통 컴포넌트

### BaseEntity
- 모든 Entity는 `BaseEntity` 상속
- 자동으로 `createdDate`, `createdId`, `updatedDate`, `updatedId` 관리
- JPA Auditing 활성화 필요 (`@EnableJpaAuditing`)

### ListRequest / ListResponse
- 페이징 처리를 위한 공통 클래스
- `ListRequest`: 페이징 파라미터 (`size`, `currentPage`)
- `ListResponse<T>`: 페이징 응답 (`size`, `currentPage`, `totalCount`, `data`)

**사용 예시:**
```java
// Request
public class {Domain}SearchRequest extends ListRequest {
    private final String keyword;
}

// Service
public ListResponse<{Domain}Response> search({Domain}SearchRequest request) {
    Page<{Domain}> page = repository.findByKeyword(
        request.getKeyword(), 
        request.toPageable()
    );
    return ListResponse.from(page, {Domain}Response::from);
}
```

## 의존성 관리

- **Java**: 21
- **Spring Boot**: 3.5.9-SNAPSHOT
- **Lombok**: Entity와 Properties에서만 사용 (DTO는 record 사용)
- **JPA**: Spring Data JPA 사용
- **Validation**: Jakarta Validation 사용

## 개발 작업 체크 포인트

1. ✅ 도메인별 패키지 구조 준수 (`domain.{domain}.{layer}`)
2. ✅ DTO는 `record`로 작성
3. ✅ Entity는 정적 팩토리 메서드로 생성
4. ✅ `@Transactional`은 Service 계층에서만 사용
5. ✅ Swagger 어노테이션으로 API 문서화
6. ✅ Validation 어노테이션으로 입력 검증
7. ✅ 코드 포맷팅 (`./gradlew spotlessApply`)
8. ✅ 단위 테스트 작성 (필요시)

## 네이밍 규칙

- **Controller**: `{Domain}Controller`
- **Service**: `{Domain}Service` (Application Service)
- **Entity**: `{Domain}` (도메인명 그대로)
- **Repository**: `{Domain}Repository`
- **Request DTO**: `{Domain}SaveRequest`, `{Domain}UpdateRequest`, `{Domain}SearchRequest`
- **Response DTO**: `{Domain}Response`
- **Exception**: `{Domain}NotFoundException`, `{Domain}ValidationException` 등

## 주의사항

- **Lombok 사용 제한**: Entity와 Properties 클래스에서만 사용, DTO는 record 사용
- **패키지 구조**: 도메인별로 수직 분리, 계층별로 수평 분리
- **의존성 방향**: Controller → Application → Domain → Infrastructure (단방향)
- **트랜잭션**: Service 계층에서만 `@Transactional` 사용
- **예외 처리**: 도메인별 Custom Exception 사용, `BaseException` 상속
