CREATE TABLE file
(
    id           BIGINT                                      NOT NULL AUTO_INCREMENT COMMENT '키',
    name         VARCHAR(255)                                NOT NULL COMMENT '이름',
    status       ENUM ('READY', 'PROGRESS', 'DONE', 'ERROR') NOT NULL DEFAULT 'READY' COMMENT '상태',
    upload_id    VARCHAR(255)                                NULL COMMENT '업로드 키',
    extension    VARCHAR(20)                                 NOT NULL COMMENT '확장자',
    server_path  VARCHAR(255)                                NOT NULL COMMENT '서버 경로',
    content_type VARCHAR(255)                                NOT NULL COMMENT '타입',
    size         BIGINT                                      NOT NULL COMMENT '크기',
    created_at   TIMESTAMP                                   NOT NULL DEFAULT NOW() COMMENT '생성일',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '파일';

CREATE TABLE config
(
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '키',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '설정';

CREATE TABLE user
(
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    name     VARCHAR(255) NOT NULL COMMENT '이름',
    is_admin BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '관리자 여부',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '유저';

CREATE TABLE account
(
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    type     VARCHAR(255) NOT NULL COMMENT '타입',
    user_id  BIGINT       NOT NULL COMMENT '유저 키',
    email    VARCHAR(255) NOT NULL COMMENT '이메일',
    username VARCHAR(255) NOT NULL COMMENT '아이디',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호',

    PRIMARY KEY (id),

    UNIQUE (email),
    UNIQUE (username),

    FOREIGN KEY (user_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '계정';

CREATE TABLE quiz_category
(
    id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    parent_id BIGINT       NULL COMMENT '부모 키',
    name      VARCHAR(255) NOT NULL COMMENT '이름',

    PRIMARY KEY (id),

    FOREIGN KEY (parent_id) REFERENCES quiz_category (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '퀴즈 분류';

CREATE TABLE quiz
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    quiz_category_id BIGINT       NOT NULL COMMENT '퀴즈 분류 키',
    title            VARCHAR(255) NOT NULL COMMENT '제목',
    content          TEXT         NULL COMMENT '내용',
    sound_file_id    BIGINT       NULL COMMENT '소리 파일 키',
    timeout_ms       BIGINT       NOT NULL COMMENT '종료 시간',
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_by_id    BIGINT       NOT NULL COMMENT '만든이 키',
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW() COMMENT '생성일',
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW() COMMENT '수정일',
    deleted_at       TIMESTAMP    NULL COMMENT '삭제일',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_category_id) REFERENCES quiz_category (id),
    FOREIGN KEY (sound_file_id) REFERENCES file (id),
    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '퀴즈';

CREATE TABLE quiz_hint
(
    id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    quiz_id BIGINT       NOT NULL COMMENT '퀴즈 키',
    name    VARCHAR(255) NOT NULL COMMENT '이름',
    content VARCHAR(255) NOT NULL COMMENT '내용',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '퀴즈 정답';

CREATE TABLE quiz_answer
(
    id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '키',
    quiz_id BIGINT       NOT NULL COMMENT '퀴즈 키',
    answer  VARCHAR(255) NOT NULL COMMENT '정답',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '퀴즈 정답';

CREATE TABLE room
(
    id              BIGINT                               NOT NULL AUTO_INCREMENT COMMENT '키',
    status          ENUM ('READY', 'PLAYING', 'DELETED') NOT NULL DEFAULT 'READY' COMMENT '상태',
    title           VARCHAR(255)                         NOT NULL COMMENT '제목',
    is_secret       BOOLEAN                              NOT NULL DEFAULT FALSE COMMENT '비밀방 여부',
    secret_password VARCHAR(255)                         NULL COMMENT '방 비밀 번호',
    created_by_id   BIGINT                               NOT NULL COMMENT '만든이 키',
    created_at      TIMESTAMP                            NOT NULL DEFAULT NOW() COMMENT '생성일',
    deleted_at      TIMESTAMP                            NULL COMMENT '삭제일',

    PRIMARY KEY (id),

    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '방';

CREATE TABLE room_quiz_category
(
    room_id          BIGINT NOT NULL COMMENT '방 키',
    quiz_category_id BIGINT NOT NULL COMMENT '퀴즈 분류 키',

    PRIMARY KEY (room_id, quiz_category_id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (quiz_category_id) REFERENCES quiz_category (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '방 퀴즈 분류';

CREATE TABLE room_quiz
(
    room_id BIGINT NOT NULL COMMENT '방 키',
    quiz_id BIGINT NOT NULL COMMENT '퀴즈 키',

    PRIMARY KEY (room_id, quiz_id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (quiz_id) REFERENCES quiz_category (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '방 퀴즈';

CREATE TABLE room_chat
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '키',
    room_id       BIGINT NOT NULL COMMENT '방 키',
    created_by_id BIGINT NOT NULL COMMENT '만든이 키',
    content       TEXT   NOT NULL COMMENT '내용',

    PRIMARY KEY (id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '방 채팅';

CREATE TABLE room_chat_answer
(
    room_chat_id BIGINT NOT NULL COMMENT '방 채팅 키',
    quiz_id      BIGINT NOT NULL COMMENT '퀴즈 키',

    PRIMARY KEY (room_chat_id, quiz_id),

    FOREIGN KEY (room_chat_id) REFERENCES room_chat (id),
    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT '방 채팅 정답';
