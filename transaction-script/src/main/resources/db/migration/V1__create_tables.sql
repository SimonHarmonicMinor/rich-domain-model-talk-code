CREATE TABLE speaker
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(200) NOT NULL,
    last_name  VARCHAR(200) NOT NULL
);

CREATE TABLE talk
(
    id         BIGSERIAL PRIMARY KEY,
    speaker_id BIGINT       NOT NULL REFERENCES speaker (id),
    status     VARCHAR(20), -- SUBMITTED/ACCEPTED/REJECTED
    title      VARCHAR(200) NOT NULL
)