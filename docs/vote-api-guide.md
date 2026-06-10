# 투표(Vote) API 프론트엔드 연동 가이드

> 대상: 프론트엔드 개발자 · 최종 업데이트: 2026-06-10
> 상세 스키마는 Swagger(`https://api.dddstudy.kr/swagger-ui/index.html`)의 **Vote** 태그 참고.

## 1. 개요

- **인증**: 모든 엔드포인트 JWT 필수 (`Authorization: Bearer <token>`).
- **응답 형식**: **성공 응답은 공통 래퍼 없이 DTO를 그대로 반환**합니다(목록은 최상위 JSON 배열). 에러만 `{ code, message, detail }` 형식입니다(5장).
- **날짜/시각**: ISO-8601 문자열(`"2026-06-10T09:00:00"`)이며 **타임존 오프셋이 없습니다**(KST 기준으로 해석하세요).
- **SDUI(Server-Driven UI)**: 서버는 질문의 "의미 타입"(`type`)과 텍스트/옵션/제약만 내려주고, **실제 렌더링은 클라이언트가 소유**합니다. 텍스트·옵션·순서·제약은 서버에서 바뀔 수 있고, 새 `type` 추가 시에만 앱 배포가 필요합니다.
- **두 영역**: 한 투표는 ① **팀 투표**(team-vote) + ② **참여 경험 피드백**(feedback)으로 구성되며, 각각 독립 템플릿/응답입니다. 둘 중 하나만 있을 수도 있습니다(`null` 가능).
- **상태머신**: `DRAFT`(작성중) → `OPEN`(진행중) → `CLOSED`(종료, 불가역). 멤버에게는 `OPEN`만 노출됩니다.
- **권한**: `[운영진]` API는 운영진(MANAGER)만 호출 가능. 멤버가 호출하면 `403 MANAGER_ONLY`.

## 2. 엔드포인트 한눈에 보기

| 구분 | 메서드 | 경로 | 설명 | 비고 |
|---|---|---|---|---|
| 멤버 | GET | `/api/votes/active` | 내 기수 진행중 투표 + 내 참여 여부 | 없으면 `404` |
| 멤버 | GET | `/api/votes/{voteId}/team-vote/template` | 팀 투표 화면 템플릿 + 팀 목록 | |
| 멤버 | GET | `/api/votes/{voteId}/feedback/template` | 피드백 화면 템플릿 | |
| 멤버 | POST | `/api/votes/{voteId}/responses` | 투표 제출(팀투표+피드백 원자 제출) | 1인 1응답 |
| 멤버 | GET | `/api/votes/{voteId}/responses/me` | 내 참여 여부 | |
| 운영진 | POST | `/api/votes` | 투표 생성(DRAFT) | |
| 운영진 | PUT | `/api/votes/{voteId}/template` | 템플릿 수정(DRAFT만) | |
| 운영진 | PATCH | `/api/votes/{voteId}/open` | 투표 시작 | |
| 운영진 | PATCH | `/api/votes/{voteId}/close` | 투표 종료 | |
| 운영진 | GET | `/api/votes` | 투표 목록(최신순) | 신규 |
| 운영진 | GET | `/api/votes/{voteId}` | 투표 상세(상태+양쪽 템플릿) | 신규 |
| 운영진 | GET | `/api/votes/{voteId}/participation` | 참여 현황(N명/N%) | |
| 운영진 | GET | `/api/votes/{voteId}/non-responders` | 미참여 명단 | |
| 운영진 | GET | `/api/votes/{voteId}/team-vote/results` | 팀투표 결과 집계 | 신규 |
| 운영진 | GET | `/api/votes/{voteId}/feedback/results` | 피드백 결과 집계 | 신규 |

> 화면별로 어떤 에러를 처리해야 하는지는 **5장 에러 매핑** 참고.

---

## 3. 멤버 흐름

### 3.1 진행중 투표 확인 — `GET /api/votes/active`
홈 화면의 "투표(NEW)" 노출/진입 판단에 사용. **진행중 투표가 없으면 `404 VOTE_NO_ACTIVE`** 이므로 404를 "투표 없음"으로 분기하세요.

```json
// 200 OK
{ "voteId": 1, "title": "DDD 13기 최종 투표", "alreadyResponded": false }
```
`alreadyResponded=true` 면 완료 화면으로 분기.

### 3.2 팀 투표 템플릿 — `GET /api/votes/{voteId}/team-vote/template`
```json
// 200 OK
{
  "templateVersion": 1,
  "status": "OPEN",
  "template": { /* TeamVoteTemplate, 6장 참고 */ },
  "teams": [
    { "teamId": 101, "name": "iOS 1팀", "serviceName": "PICKFLOW", "isOwnTeam": false },
    { "teamId": 105, "name": "iOS 5팀", "serviceName": "MOABO",    "isOwnTeam": true }
  ]
}
```
- `isOwnTeam=true` 인 팀은 **선택 불가**(본인 팀). UI에서 비활성 처리.
- `serviceName` 은 데이터 미적재 시 `null` 일 수 있습니다.

### 3.3 피드백 템플릿 — `GET /api/votes/{voteId}/feedback/template`
```json
// 200 OK
{ "templateVersion": 1, "status": "OPEN", "template": { /* FeedbackTemplate, 6장 참고 */ } }
```

### 3.4 투표 제출 — `POST /api/votes/{voteId}/responses`
팀투표 + 피드백을 **한 번에** 제출합니다. `OPEN` 상태에서만 가능, **1인 1응답(재투표 불가)**.

```json
// Request Body
{
  "teamVote": [
    { "categoryId": "PLANNING", "teamIds": [101, 102], "reason": "기획 흐름이 명확했어요" }
  ],
  "feedback": [
    { "questionId": "BEST_CURRICULUM", "optionIds": ["OPT_A"], "textValue": null, "boolValue": null },
    { "questionId": "RECOMMEND",       "optionIds": null,      "textValue": null, "boolValue": true },
    { "questionId": "FREE_FEEDBACK",   "optionIds": null,      "textValue": "좋은 경험이었습니다", "boolValue": null }
  ]
}
// 201 Created (본문 없음)
```
- **질문 타입별로 채우는 필드가 다릅니다**(타입과 안 맞는 값을 같이 보내면 `400 VOTE_ANSWER_INVALID`):
  `MULTI_SELECT` → `optionIds` / `LONG_TEXT` → `textValue` / `BOOLEAN` → `boolValue`
- **한쪽 영역만 있는 투표**는 없는 쪽을 빈 배열(`[]`)로 보내거나 생략하면 됩니다(`teamVote`/`feedback` 둘 다 nullable).
- 서버가 제약(선택 개수/본인 팀 제외/글자수/필수/모든 부문 1팀 이상)을 **재검증**합니다. 클라이언트 검증은 UX용이며 신뢰 경계는 서버입니다.

### 3.5 내 참여 여부 — `GET /api/votes/{voteId}/responses/me`
```json
// 200 OK
{ "voteId": 1, "responded": true }
```

---

## 4. 운영진 흐름

### 4.1 투표 목록 (신규) — `GET /api/votes`
본인 기수의 전체 투표를 최신순으로 반환. 관리 화면 진입점. 투표가 없으면 `[]`.
```json
// 200 OK
[
  { "voteId": 2, "title": "임시 투표", "status": "DRAFT", "openedAt": null, "closedAt": null, "createdDate": "2026-06-10T09:00:00" },
  { "voteId": 1, "title": "DDD 13기 최종 투표", "status": "OPEN", "openedAt": "2026-06-10T10:00:00", "closedAt": null, "createdDate": "2026-06-09T15:00:00" }
]
```

### 4.2 투표 상세 (신규) — `GET /api/votes/{voteId}`
상태 + 양쪽 템플릿 전체. DRAFT 편집 재개 화면에 사용.
```json
// 200 OK
{
  "voteId": 1,
  "title": "DDD 13기 최종 투표",
  "status": "DRAFT",
  "templateVersion": 2,
  "teamVoteTemplate": { /* TeamVoteTemplate, null 가능 */ },
  "feedbackTemplate": { /* FeedbackTemplate, null 가능 */ }
}
```

### 4.3 참여 현황 — `GET /api/votes/{voteId}/participation`
참여율 모집단은 **운영진 제외(MEMBER)**.
```json
// 200 OK
{ "voteId": 1, "title": "DDD 13기 최종 투표", "status": "OPEN", "totalMembers": 42, "respondedMembers": 35, "participationRate": 83 }
```

### 4.4 미참여 명단 — `GET /api/votes/{voteId}/non-responders`
미참여자가 없으면 `totalCount: 0, members: []`.
```json
// 200 OK
{ "totalCount": 7, "members": [ { "memberId": 11, "name": "홍길동", "teamName": "iOS 1팀" } ] }
```

### 4.5 팀투표 결과 집계 (신규) — `GET /api/votes/{voteId}/team-vote/results`
부문별 팀 득표 순위 + 작성 사유(익명). `OPEN`/`CLOSED` 모두 실시간 조회(DRAFT는 빈 결과).
```json
// 200 OK
{
  "voteId": 1,
  "title": "DDD 13기 최종 투표",
  "status": "CLOSED",
  "totalResponses": 35,
  "categories": [
    {
      "categoryId": "PLANNING",
      "title": "기획 완성도",
      "order": 1,
      "teams": [
        { "rank": 1, "teamId": 101, "name": "iOS 1팀", "serviceName": "PICKFLOW", "voteCount": 12 },
        { "rank": 2, "teamId": 102, "name": "iOS 2팀", "serviceName": "MOABO",    "voteCount": 8 }
      ],
      "reasons": ["기획이 탄탄했어요", "사용자 흐름이 명확함"]
    }
  ]
}
```
- `teams` 는 **득표수 내림차순**, `rank` 는 1부터. **0표 팀은 포함되지 않습니다.**
- `reasons` 는 해당 부문에 작성된 사유 목록(작성자 식별 불가, 익명).

### 4.6 피드백 결과 집계 (신규) — `GET /api/votes/{voteId}/feedback/results`
질문 타입별로 응답 형태가 다릅니다. **사용하지 않는 필드는 `null`** 입니다. `followUp`(중첩 질문)도 평탄화되어 함께 내려옵니다.
```json
// 200 OK
{
  "voteId": 1,
  "totalResponses": 35,
  "questions": [
    {
      "questionId": "BEST_CURRICULUM", "title": "가장 좋았던 커리큘럼", "type": "MULTI_SELECT", "order": 1,
      "options": [
        { "optionId": "OPT_A", "label": "JPA 심화", "count": 20 },
        { "optionId": "OPT_B", "label": "테스트 코드", "count": 0 }
      ],
      "trueCount": null, "falseCount": null, "textAnswers": null
    },
    {
      "questionId": "RECOMMEND", "title": "다음 기수에 추천하시겠어요?", "type": "BOOLEAN", "order": 2,
      "options": null, "trueCount": 30, "falseCount": 5, "textAnswers": null
    },
    {
      "questionId": "FREE_FEEDBACK", "title": "자유 피드백", "type": "LONG_TEXT", "order": 3,
      "options": null, "trueCount": null, "falseCount": null,
      "textAnswers": ["좋은 경험이었습니다", "감사합니다"]
    }
  ]
}
```
- `MULTI_SELECT` → `options[]`. **템플릿의 모든 선택지가 0 포함 분포로** 내려옵니다(차트용).
- `BOOLEAN` → `trueCount`/`falseCount`.
- `LONG_TEXT` → `textAnswers[]`(익명).

### 4.7 투표 생성 — `POST /api/votes`
템플릿을 포함한 전체 요청 바디 예시입니다. `teamVoteTemplate`/`feedbackTemplate` 중 하나만 넣어도 됩니다(`null` 가능). 템플릿 필드 의미는 6장 참고.
```json
// Request Body
{
  "generationId": 13,
  "title": "DDD 13기 최종 투표",
  "teamVoteTemplate": {
    "title": "팀 투표",
    "description": "함께한 팀들을 평가해주세요",
    "notice": "본인 팀은 선택할 수 없어요",
    "categories": [
      {
        "id": "PLANNING", "order": 1, "title": "기획 완성도",
        "maxSelectableTeams": 2, "reasonRequired": false,
        "reasonMinLength": 0, "reasonMaxLength": 500, "reasonLabel": "선택 이유"
      }
    ]
  },
  "feedbackTemplate": {
    "title": "참여 경험 피드백",
    "description": "솔직한 의견을 들려주세요",
    "questions": [
      {
        "id": "BEST_CURRICULUM", "order": 1, "type": "MULTI_SELECT", "title": "가장 좋았던 커리큘럼",
        "helpText": "최대 2개", "required": true, "maxSelectableOptions": 2, "maxLength": null,
        "options": [ { "id": "OPT_A", "label": "JPA 심화" }, { "id": "OPT_B", "label": "테스트 코드" } ],
        "followUp": null
      },
      {
        "id": "RECOMMEND", "order": 2, "type": "BOOLEAN", "title": "다음 기수에 추천하시겠어요?",
        "helpText": null, "required": true, "maxSelectableOptions": null, "maxLength": null,
        "options": null, "followUp": null
      }
    ]
  }
}
// 201 Created
{ "voteId": 1 }
```

### 4.8 수정/시작/종료
- `PUT /api/votes/{voteId}/template` — Body는 `{ teamVoteTemplate, feedbackTemplate }`(생성과 동일 구조). **DRAFT 상태만** 가능, 수정 시 `templateVersion` 증가.
- `PATCH /api/votes/{voteId}/open` — DRAFT → OPEN.
- `PATCH /api/votes/{voteId}/close` — OPEN → CLOSED (불가역).

---

## 5. 에러

성공은 DTO를 직접 반환하지만, **에러는 모두 아래 형식**입니다.
```json
{ "code": "VOTE_NO_ACTIVE", "message": "진행 중인 투표가 없습니다.", "detail": null }
```

### 5.1 엔드포인트별 발생 에러 (화면 분기용)

| API | 발생 가능 에러 (status `code`) |
|---|---|
| `GET /active` | `404 VOTE_NO_ACTIVE`(진행중 없음) |
| `POST /{id}/responses` (제출) | `400 VOTE_NOT_OPEN`, `400 VOTE_OWN_TEAM_SELECTED`, `400 VOTE_ANSWER_INVALID`, `400 VOTE_ALREADY_RESPONDED`(재참여), `403 VOTE_MANAGER_NOT_ALLOWED`(운영진) |
| `POST /votes` (생성) | `403 MANAGER_ONLY` |
| `PUT /{id}/template` (수정) | `400 VOTE_NOT_DRAFT`, `403 MANAGER_ONLY` |
| `PATCH /{id}/open` | `400 VOTE_INVALID_STATUS`(DRAFT 아님), `400 VOTE_ALREADY_OPEN`(기수에 이미 OPEN), `403 MANAGER_ONLY` |
| `PATCH /{id}/close` | `400 VOTE_INVALID_STATUS`(OPEN 아님), `403 MANAGER_ONLY` |
| 운영진 조회(목록/상세/현황/명단/결과) | `403 MANAGER_ONLY`, `404 VOTE_NOT_FOUND`(없는 voteId) |
| 공통 | `401`(토큰 만료/누락 → 재로그인), `500`(서버 오류) |

### 5.2 에러 코드 사전

| 상태 | code | 의미 |
|---|---|---|
| 400 | `VOTE_NOT_DRAFT` | DRAFT 아닌데 템플릿 수정 |
| 400 | `VOTE_NOT_OPEN` | OPEN 아닌데 제출 |
| 400 | `VOTE_INVALID_STATUS` | 잘못된 상태 전이(open/close) |
| 400 | `VOTE_OWN_TEAM_SELECTED` | 본인 팀을 선택 |
| 400 | `VOTE_ANSWER_INVALID` | 응답이 제약 위반(개수/글자수/필수/타입 불일치 등) |
| 400 | `VOTE_ALREADY_RESPONDED` | 이미 참여한 멤버의 재제출 |
| 400 | `VOTE_ALREADY_OPEN` | 같은 기수에 이미 OPEN 투표 존재(open 시) |
| 403 | `VOTE_MANAGER_NOT_ALLOWED` | 운영진이 투표 제출 시도(운영진은 투표 대상 아님) |
| 403 | `MANAGER_ONLY` | 멤버가 운영진 전용 API 호출 |
| 404 | `VOTE_NOT_FOUND` | 존재하지 않는 voteId |
| 404 | `VOTE_NO_ACTIVE` | `/active` 진행중 투표 없음 |

---

## 6. SDUI 템플릿 구조

`id`(부문/질문/선택지)는 응답·집계에 **그대로 사용**하는 고유 키입니다. 화면 표기는 `title`/`label`을 쓰세요.

### 6.1 TeamVoteTemplate (팀 투표)
```json
{
  "title": "팀 투표",
  "description": "함께한 팀들을 평가해주세요",
  "notice": "본인 팀은 선택할 수 없어요",
  "categories": [
    {
      "id": "PLANNING",
      "order": 1,
      "title": "기획 완성도",
      "maxSelectableTeams": 2,    // 최대 선택 팀 수
      "reasonRequired": false,    // 팀 선택 시 사유 필수 여부
      "reasonMinLength": 0,
      "reasonMaxLength": 500,
      "reasonLabel": "선택 이유"
    }
  ]
}
```

### 6.2 FeedbackTemplate (참여 경험 피드백)
```json
{
  "title": "참여 경험 피드백",
  "description": "솔직한 의견을 들려주세요",
  "questions": [
    {
      "id": "BEST_CURRICULUM",
      "order": 1,
      "type": "MULTI_SELECT",     // TEAM_SELECT | MULTI_SELECT | LONG_TEXT | BOOLEAN
      "title": "가장 좋았던 커리큘럼",
      "helpText": "최대 2개",
      "required": true,
      "maxSelectableOptions": 2,  // MULTI_SELECT 최대 선택 수 (null=무제한)
      "maxLength": null,          // LONG_TEXT 최대 글자수
      "options": [                // MULTI_SELECT 선택지
        { "id": "OPT_A", "label": "JPA 심화" },
        { "id": "OPT_B", "label": "테스트 코드" }
      ],
      "followUp": null            // 선택적 후속 질문(같은 Question 구조, 재귀 중첩 가능)
    }
  ]
}
```

### 6.3 컴포넌트 타입(VoteComponentType)
| type | 의미 | 제출 시 채울 필드 | 결과 집계 형태 |
|---|---|---|---|
| `TEAM_SELECT` | 팀 선택 | (팀투표 영역 전용) | team-vote/results |
| `MULTI_SELECT` | 다중 선택 | `optionIds` | `options[].count` |
| `LONG_TEXT` | 장문 텍스트 | `textValue` | `textAnswers[]` |
| `BOOLEAN` | 예/아니오 | `boolValue` | `trueCount`/`falseCount` |

---

## 7. 알아둘 점

- **멤버용 "결과 공개" API는 아직 없습니다.** 결과 집계(`/results`)는 운영진 전용입니다. 멤버가 결과를 보는 화면이 디자인에 있다면 백엔드에 별도 요청이 필요합니다.
- `serviceName` 은 데이터 적재 전까지 `null` 일 수 있습니다(결과/목록/팀 목록 공통).
