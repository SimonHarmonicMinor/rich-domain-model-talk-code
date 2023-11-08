CREATE TABLE outbox_talk_rejected
(
    id     BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL, -- CREATED, PROCESSED
    talkId BIGINT      NOT NULL
);