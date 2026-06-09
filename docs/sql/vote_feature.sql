-- =====================================================================
-- Vote(투표) 기능 첫 배포용 수동 DDL (MySQL 8)
-- =====================================================================
-- 이 프로젝트는 spring.jpa.hibernate.ddl-auto=update 이며 Flyway/Liquibase 가 없다.
-- update 모드는 "새 테이블"은 제약과 함께 생성하지만, "이미 존재하는 테이블"에는
-- 제약 추가/컬럼 타입 변경을 신뢰성 있게 반영하지 못한다.
-- 따라서 운영 배포 시 ddl-auto 에 의존하지 말고 본 스크립트를 먼저 1회 실행할 것을 권장한다.
-- (특히 vote_response 의 유니크 제약, team.service_name 컬럼 추가)
-- =====================================================================

-- 0) 기존 team 테이블에 서비스명 컬럼 추가 (nullable -> update 모드에서도 안전하지만 명시 실행 권장)
ALTER TABLE team
    ADD COLUMN service_name VARCHAR(100) NULL COMMENT '서비스명 (투표 화면 표기용)';

-- 1) 투표 (라이프사이클/상태/기수 소유, 두 관심사의 템플릿 보유)
CREATE TABLE vote (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    generation_id     BIGINT       NOT NULL COMMENT '기수 Id',
    title             VARCHAR(100) NOT NULL COMMENT '투표 제목',
    status            VARCHAR(20)  NOT NULL COMMENT '투표 상태 (DRAFT/OPEN/CLOSED)',
    template_version  INT          NOT NULL COMMENT '템플릿 버전',
    team_vote_template TEXT        NULL COMMENT '팀 투표 템플릿 (JSON)',
    feedback_template  TEXT        NULL COMMENT '참여 경험 피드백 템플릿 (JSON)',
    opened_at         DATETIME     NULL COMMENT '투표 시작 시각',
    closed_at         DATETIME     NULL COMMENT '투표 종료 시각',
    created_date      DATETIME     NOT NULL COMMENT '생성 일자',
    created_id        BIGINT       NOT NULL COMMENT '생성자',
    updated_date      DATETIME     NULL COMMENT '수정 일자',
    updated_id        BIGINT       NULL COMMENT '수정자',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 2) 투표 응답 (투표당 1인 1응답 - 유니크 제약으로 멱등 보장)
CREATE TABLE vote_response (
    id                     BIGINT NOT NULL AUTO_INCREMENT,
    vote_id                BIGINT NOT NULL COMMENT '투표 Id',
    member_id              BIGINT NOT NULL COMMENT '응답한 멤버 Id',
    template_version       INT    NOT NULL COMMENT '응답 시점 템플릿 버전',
    snapshot_team_id       BIGINT NULL COMMENT '제출 시점 소속 팀 Id 스냅샷',
    snapshot_generation_id BIGINT NULL COMMENT '제출 시점 소속 기수 Id 스냅샷',
    created_date           DATETIME NOT NULL COMMENT '생성 일자',
    created_id             BIGINT   NOT NULL COMMENT '생성자',
    updated_date           DATETIME NULL COMMENT '수정 일자',
    updated_id             BIGINT   NULL COMMENT '수정자',
    PRIMARY KEY (id),
    CONSTRAINT uk_vote_response_vote_member UNIQUE (vote_id, member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3) 팀 투표 선택 (부문별 선택 팀, 집계용 정규화 행)
CREATE TABLE team_vote_answer (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    response_id  BIGINT       NOT NULL COMMENT '응답 Id',
    category_id  VARCHAR(100) NOT NULL COMMENT '부문 ID(템플릿 안정 시맨틱 ID)',
    team_id      BIGINT       NOT NULL COMMENT '선택한 팀 Id',
    created_date DATETIME     NOT NULL COMMENT '생성 일자',
    created_id   BIGINT       NOT NULL COMMENT '생성자',
    updated_date DATETIME     NULL COMMENT '수정 일자',
    updated_id   BIGINT       NULL COMMENT '수정자',
    PRIMARY KEY (id),
    KEY idx_team_vote_answer_response (response_id),
    KEY idx_team_vote_answer_category_team (category_id, team_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 4) 팀 투표 부문별 작성 사유
CREATE TABLE team_vote_reason (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    response_id  BIGINT       NOT NULL COMMENT '응답 Id',
    category_id  VARCHAR(100) NOT NULL COMMENT '부문 ID(템플릿 안정 시맨틱 ID)',
    reason       VARCHAR(500) NULL COMMENT '작성 사유',
    created_date DATETIME     NOT NULL COMMENT '생성 일자',
    created_id   BIGINT       NOT NULL COMMENT '생성자',
    updated_date DATETIME     NULL COMMENT '수정 일자',
    updated_id   BIGINT       NULL COMMENT '수정자',
    PRIMARY KEY (id),
    KEY idx_team_vote_reason_response (response_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 5) 피드백 응답 (질문 타입별로 option_id / text_value / bool_value 중 하나가 채워짐)
CREATE TABLE feedback_answer (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    response_id  BIGINT       NOT NULL COMMENT '응답 Id',
    question_id  VARCHAR(100) NOT NULL COMMENT '질문 ID(템플릿 안정 시맨틱 ID)',
    option_id    VARCHAR(100) NULL COMMENT '선택지 ID(MULTI_SELECT)',
    text_value   VARCHAR(500) NULL COMMENT '텍스트 응답(LONG_TEXT)',
    bool_value   BIT(1)       NULL COMMENT '예/아니오 응답(BOOLEAN)',
    created_date DATETIME     NOT NULL COMMENT '생성 일자',
    created_id   BIGINT       NOT NULL COMMENT '생성자',
    updated_date DATETIME     NULL COMMENT '수정 일자',
    updated_id   BIGINT       NULL COMMENT '수정자',
    PRIMARY KEY (id),
    KEY idx_feedback_answer_response (response_id),
    KEY idx_feedback_answer_question_option (question_id, option_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
