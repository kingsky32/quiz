create TABLE file
(
    id           BIGINT                                      NOT NULL AUTO_INCREMENT comment '키',
    name         VARCHAR(255)                                NOT NULL comment '이름',
    status       ENUM ('READY', 'PROGRESS', 'DONE', 'ERROR') NOT NULL DEFAULT 'READY' comment '상태',
    upload_id    VARCHAR(255)                                NULL comment '업로드 키',
    extension    VARCHAR(20)                                 NOT NULL comment '확장자',
    server_path  VARCHAR(255)                                NOT NULL comment '서버 경로',
    content_type VARCHAR(255)                                NOT NULL comment '타입',
    size         BIGINT                                      NOT NULL comment '크기',
    created_at   TIMESTAMP                                   NOT NULL DEFAULT NOW() comment '생성일',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '파일';

create TABLE config
(
    id BIGINT NOT NULL AUTO_INCREMENT comment '키',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '설정';

create TABLE user
(
    id       BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    name     VARCHAR(255) NOT NULL comment '이름',
    is_admin BOOLEAN      NOT NULL DEFAULT FALSE comment '관리자 여부',

    PRIMARY KEY (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '유저';

create TABLE account
(
    id       BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    type     VARCHAR(255) NOT NULL comment '타입',
    user_id  BIGINT       NOT NULL comment '유저 키',
    email    VARCHAR(255) NOT NULL comment '이메일',
    username VARCHAR(255) NOT NULL comment '아이디',
    password VARCHAR(255) NOT NULL comment '비밀번호',

    PRIMARY KEY (id),

    UNIQUE (email),
    UNIQUE (username),

    FOREIGN KEY (user_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '계정';

create TABLE quiz_category
(
    id        BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    parent_id BIGINT       NULL comment '부모 키',
    name      VARCHAR(255) NOT NULL comment '이름',

    PRIMARY KEY (id),

    FOREIGN KEY (parent_id) REFERENCES quiz_category (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '퀴즈 분류';

create TABLE quiz
(
    id               BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    quiz_category_id BIGINT       NOT NULL comment '퀴즈 분류 키',
    title            VARCHAR(255) NOT NULL comment '제목',
    content          TEXT         NULL comment '내용',
    sound_file_id    BIGINT       NULL comment '소리 파일 키',
    timeout_ms       BIGINT       NOT NULL comment '종료 시간',
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE comment '활성화 여부',
    created_by_id    BIGINT       NOT NULL comment '만든이 키',
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW() comment '생성일',
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW() comment '수정일',
    deleted_at       TIMESTAMP    NULL comment '삭제일',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_category_id) REFERENCES quiz_category (id),
    FOREIGN KEY (sound_file_id) REFERENCES file (id),
    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '퀴즈';

create TABLE quiz_hint
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    quiz_id             BIGINT       NOT NULL comment '퀴즈 키',
    name                VARCHAR(255) NOT NULL comment '이름',
    content             VARCHAR(255) NOT NULL comment '내용',
    exposed_remain_time BIGINT       NOT NULL comment '노출 남은 시간',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '퀴즈 정답';

create TABLE quiz_answer
(
    id      BIGINT       NOT NULL AUTO_INCREMENT comment '키',
    quiz_id BIGINT       NOT NULL comment '퀴즈 키',
    answer  VARCHAR(255) NOT NULL comment '정답',

    PRIMARY KEY (id),

    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '퀴즈 정답';

create TABLE room
(
    id              BIGINT                               NOT NULL AUTO_INCREMENT comment '키',
    status          ENUM ('READY', 'PLAYING', 'DELETED') NOT NULL DEFAULT 'READY' comment '상태',
    title           VARCHAR(255)                         NOT NULL comment '제목',
    current_number  BIGINT                               NOT NULL comment '현재 문제',
    number_of_quiz  BIGINT                               NOT NULL comment '문제 갯수',
    is_secret       BOOLEAN                              NOT NULL DEFAULT FALSE comment '비밀방 여부',
    secret_password VARCHAR(255)                         NULL comment '방 비밀 번호',
    created_by_id   BIGINT                               NOT NULL comment '만든이 키',
    created_at      TIMESTAMP                            NOT NULL DEFAULT NOW() comment '생성일',
    deleted_at      TIMESTAMP                            NULL comment '삭제일',

    PRIMARY KEY (id),

    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '방';

create TABLE room_quiz_category
(
    room_id          BIGINT NOT NULL comment '방 키',
    quiz_category_id BIGINT NOT NULL comment '퀴즈 분류 키',

    PRIMARY KEY (room_id, quiz_category_id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (quiz_category_id) REFERENCES quiz_category (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '방 퀴즈 분류';

create TABLE room_quiz
(
    id         BIGINT AUTO_INCREMENT NOT NULL comment '키',
    room_id    BIGINT                NOT NULL comment '방 키',
    quiz_id    BIGINT                NOT NULL comment '퀴즈 키',
    created_at TIMESTAMP             NOT NULL DEFAULT NOW() comment '생성일',

    PRIMARY KEY (id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (quiz_id) REFERENCES quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '방 퀴즈';

create TABLE room_chat
(
    id            BIGINT   NOT NULL AUTO_INCREMENT comment '키',
    room_id       BIGINT   NOT NULL comment '방 키',
    created_by_id BIGINT   NOT NULL comment '만든이 키',
    content       TEXT     NOT NULL comment '내용',
    created_at    DATETIME NOT NULL DEFAULT NOW() comment '생성일',

    PRIMARY KEY (id),

    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (created_by_id) REFERENCES user (id)
) DEFAULT CHARACTER SET
      = utf8mb4
  COLLATE = utf8mb4_bin comment '방 채팅';

create TABLE room_chat_answer
(
    room_chat_id BIGINT NOT NULL comment '방 채팅 키',
    room_quiz_id BIGINT NOT NULL comment '방 퀴즈 키',

    PRIMARY KEY (room_chat_id, room_quiz_id),

    FOREIGN KEY (room_chat_id) REFERENCES room_chat (id),
    FOREIGN KEY (room_quiz_id) REFERENCES room_quiz (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin comment '방 채팅 정답';
