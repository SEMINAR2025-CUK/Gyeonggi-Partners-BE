-- V1__init.sql
-- Initial database schema setup

-- ENUM 타입 정의
-- ENUM types are defined first as they are used in the table definitions.
CREATE TYPE user_role_enum AS ENUM (
    'USER',     -- 일반 시민 사용자
    'OFFICIAL', -- 인증된 공무원 사용자
    'ADMIN'     -- 시스템 관리자
);
CREATE TYPE region_enum AS ENUM (
    'SUWON', 'SEONGNAM', 'UIJEONGBU', 'ANYANG', 'BUCHEON', 'GWANGMYEONG',
    'PYEONGTAEK', 'DONGDUCHEON', 'ANSAN', 'GOYANG', 'GWACHEON', 'GURI',
    'NAMYANGJU', 'OSAN', 'SIHEUNG', 'GUNPO', 'UIWANG', 'HANAM', 'YONGIN',
    'PAJU', 'ICHEON', 'ANSEONG', 'GIMPO', 'HWASEONG', 'GWANGJU', 'YANGJU',
    'POCHEON', 'YEOJU'
);
CREATE TYPE access_level_enum AS ENUM (
    'PUBLIC',            -- 누구나 참여 가능
    'OFFICIALS_ONLY',  -- 공무원만 참여 가능
    'USER_ONLY'
);
CREATE TYPE message_type_enum AS ENUM (
    'TEXT',     -- 일반 텍스트 메시지
    'IMAGE',    -- 이미지 메시지
    'ENTRY',    -- 입장 알림 메시지 (시스템)
    'EXIT'      -- 퇴장 알림 메시지 (시스템)
);

CREATE TYPE proposal_status_enum AS ENUM (
    'DRAFTING',         -- 내용 작성 중 (Locked)
    'SAVING',           -- 미 완성 상태 (unLocked)
    'COMPLETED',        -- 완성 상태 (Locked)
    'VOTING',           -- 동의 진행 중 (Locked)
    'READY_TO_SUBMIT',  -- 제출 가능 (Locked)
    'SUBMITTED'         -- 외부 시스템에 제출 완료 (unLocked)
);


-- 사용자 테이블 (users)
-- Stores user account information.
CREATE TABLE users
(
    user_id        BIGSERIAL PRIMARY KEY,
    login_id       VARCHAR(30) UNIQUE NOT NULL,
    login_pw       VARCHAR(255)       NOT NULL,
    name           VARCHAR(50)        NOT NULL,
    nickname       VARCHAR(50) UNIQUE NOT NULL,
    email          VARCHAR(50) UNIQUE NOT NULL,
    phone_number   VARCHAR(20) UNIQUE NOT NULL,
    role           VARCHAR(20)     NOT NULL DEFAULT 'USER',
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE,
    deleted_at     TIMESTAMP WITH TIME ZONE
);
COMMENT ON TABLE users IS '사용자 정보를 저장하는 테이블';
COMMENT ON COLUMN users.login_pw IS '비밀번호는 해시 처리하여 저장';


-- 논의방 테이블 (discussion_rooms)
-- Represents a chat room or a space for discussion.
CREATE TABLE discussion_rooms
(
    room_id         BIGSERIAL PRIMARY KEY,
    title           VARCHAR(100)      NOT NULL,
    description     VARCHAR(255),
    region          region_enum NOT NULL,
    access_level    access_level_enum NOT NULL DEFAULT 'PUBLIC',
    version         BIGINT            NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE,
    deleted_at      TIMESTAMP WITH TIME ZONE
);
COMMENT ON TABLE discussion_rooms IS '논의가 이루어지는 공간(채팅방) 테이블';


-- 멤버 테이블 (members)
-- Junction table linking users and discussion_rooms (Many-to-Many).
CREATE TABLE members
(
    member_id       BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    room_id         BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_members_discussion_room FOREIGN KEY (room_id) REFERENCES discussion_rooms (room_id) ON DELETE CASCADE,
    UNIQUE (user_id, room_id) -- 한 사용자는 한 논의방에 한 번만 참여 가능
);
COMMENT ON TABLE members IS '사용자와 논의방의 다대다 관계를 위한 조인 테이블';


-- 채팅 테이블 (chats)
-- Stores all messages sent within discussion rooms.
CREATE TABLE chat
(
    chat_id         BIGSERIAL PRIMARY KEY,
    room_id         BIGINT            NOT NULL,
    sender_id       BIGINT            NOT NULL,
    content         TEXT              NOT NULL,
    chat_type       message_type_enum    NOT NULL DEFAULT 'TEXT',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_discussion_room FOREIGN KEY (room_id) REFERENCES discussion_rooms (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE SET NULL -- 발신자 탈퇴 시 메시지는 남도록 SET NULL 처리
);
COMMENT ON TABLE chat IS '논의방에서 주고받은 메시지 정보를 저장하는 테이블';
COMMENT ON COLUMN chat.sender_id IS '메시지를 보낸 사용자 ID';


-- 솔루션 제안서 테이블 (proposals)
-- Stores proposals submitted within a discussion room.
CREATE TABLE proposals
(
    proposal_id       BIGSERIAL PRIMARY KEY,
    room_id           BIGINT            NOT NULL,
    last_modifier_id         BIGINT,
    title             VARCHAR(100)      NOT NULL,

    problem_overview  TEXT              NOT NULL,
    solution          TEXT              NOT NULL,
    evidences         JSONB             DEFAULT '[]'::jsonb,

    required_consents INTEGER           NOT NULL DEFAULT 1,
    consent_deadline  TIMESTAMP WITH TIME ZONE,
    status            proposal_status_enum NOT NULL DEFAULT 'DRAFTING',

    locked_by         BIGINT,
    locked_at         TIMESTAMP WITH TIME ZONE,

    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE,
    deleted_at        TIMESTAMP WITH TIME ZONE,

    -- 외래 키 제약조건
    CONSTRAINT fk_proposals_discussion_room FOREIGN KEY (room_id) REFERENCES discussion_rooms (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_proposals_author FOREIGN KEY (last_modifier_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_proposals_locked_user FOREIGN KEY (locked_by) REFERENCES users (user_id) ON DELETE SET NULL
);


CREATE TABLE proposal_consents
(
    proposal_id       BIGINT            NOT NULL,
    user_id           BIGINT            NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- [핵심] 복합 키(Composite Key) 설정
    -- 1. 식별자 역할: 별도의 ID 컬럼 없이 이 두 조합으로 식별
    -- 2. 중복 방지: (1번 제안서, A유저) 조합이 두 번 들어갈 수 없음 (UNIQUE 효과)
    PRIMARY KEY (proposal_id, user_id),

    -- 외래 키 제약조건
    -- 제안서가 삭제되면 투표 내역도 같이 삭제 (CASCADE)
    CONSTRAINT fk_consents_proposal FOREIGN KEY (proposal_id) REFERENCES proposals (proposal_id) ON DELETE CASCADE,
    -- 유저가 탈퇴해도 투표 수는 유지하고 싶다면 ON DELETE SET NULL 등을 고려해야 하나,
    -- 보통 복합키 구성원인 경우 CASCADE로 함께 지우거나, 유저 테이블에서 Soft Delete를 하는 것이 일반적임.
    CONSTRAINT fk_consents_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

COMMENT ON TABLE proposals IS '논의방 내에서 제출된 제안서를 관리하는 테이블';
COMMENT ON COLUMN proposals.last_modifier_id IS '제안서를 작성한 사용자 ID';