package com.example.demo.service;

import com.example.demo.config.JdbiConfig;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Testcontainers
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = NONE)
@Import({JdbiConfig.class, TalkService.class})
class TalkServiceTest {
    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:13");

    @Autowired
    private Jdbi jdbi;
    @Autowired
    private TalkService talkService;

    @Test
    void shouldSucceed() {
        var speakerId = jdbi.withHandle(
            h -> h.createUpdate("INSERT INTO speaker (first_name, last_name) VALUES ('', '')")
                     .executeAndReturnGeneratedKeys("id")
                     .mapTo(Long.class)
                     .one()
        );

        talkService.submitTalk(speakerId, "title");

    }
}