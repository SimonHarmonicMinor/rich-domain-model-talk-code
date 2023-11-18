package com.example.demo.transaction_script_service;

import com.example.demo.exception.CannotAcceptTalkException;
import com.example.demo.exception.CannotSubmitTalkException;
import org.jdbi.v3.core.Jdbi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TalkService {
    private final Jdbi jdbi;
    private final TalkRejectedGateway talkRejectedGateway;

    public TalkSubmittedResult submitTalk(Long speakerId, String title) {
        var talkId = jdbi.inTransaction(handle -> {
            // lock speaker
            handle.createUpdate("SELECT id FROM speaker WHERE id = :id FOR UPDATE")
                .bind("id", speakerId)
                .execute();
            var acceptedTalksCount =
                handle.select("SELECT count(*) FROM talk WHERE speaker_id = :id AND status = 'ACCEPTED'")
                    .bind("id", speakerId)
                    .mapTo(Long.class)
                    .one();
            // whether speaker is experienced
            var experienced = acceptedTalksCount >= 10;
            var maxSubmittedTalksCount = experienced ? 3 : 5;
            var submittedTalksCount =
                handle.select("SELECT count(*) FROM talk WHERE speaker_id = :id AND status = 'SUBMITTED'")
                    .bind("id", speakerId)
                    .mapTo(Long.class)
                    .one();
            if (submittedTalksCount >= maxSubmittedTalksCount) {
                throw new CannotSubmitTalkException("Submitted talks count is maximum: " + maxSubmittedTalksCount);
            }
            return handle.createUpdate(
                    """
                        INSERT INTO talk (speaker_id, status, title)
                        VALUES (:id, 'SUBMITTED', :title)
                        """
                ).bind("id", speakerId)
                       .bind("title", title)
                       .executeAndReturnGeneratedKeys("id")
                       .mapTo(Long.class)
                       .one();
        });
        return new TalkSubmittedResult(talkId);
    }

    public void acceptTalk(Long talkId) {
        jdbi.useTransaction(handle -> {
            var status =
                handle.select("SELECT status FROM talk WHERE id = :id")
                    .bind("id", talkId)
                    .mapTo(String.class)
                    .one();
            if (!status.equals("SUBMITTED")) {
                throw new CannotAcceptTalkException("Cannot accept a talk because its status is: " + status);
            }
            handle.createUpdate("UPDATE talk SET status = 'ACCEPT' WHERE id = :id")
                .bind("id", talkId)
                .execute();
        });
    }

    public void rejectTalk(Long talkId) {
        jdbi.useTransaction(handle -> {
            // code fore checking status...
            var status =
                handle.select("SELECT status FROM talk WHERE id = :id")
                    .bind("id", talkId)
                    .mapTo(String.class)
                    .one();
            if (!status.equals("SUBMITTED")) {
                throw new CannotAcceptTalkException("Cannot accept a talk because its status is: " + status);
            }
            handle.createUpdate("UPDATE talk SET status = 'REJECTED' WHERE id = :id")
                .bind("id", talkId)
                .execute();
            handle.createUpdate(
                    """
                        INSERT INTO outbox_talk_rejected (status, talkId)
                        VALUES ('CREATED', :talkId)
                        """
                ).bind("talkId", talkId)
                .execute();
        });
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        jdbi.useTransaction(handle -> {
            var outboxRecords =
                handle.select("""
                        SELECT * FROM outbox_talk_rejected
                        WHERE status = 'CREATED'
                        FOR UPDATE SKIP LOCKED
                        """)
                    .mapToMap()
                    .list();
            for (Map<String, Object> outboxRecord : outboxRecords) {
                talkRejectedGateway.notifyTalkRejection((Long) outboxRecord.get("talkId"));
            }
            handle.createUpdate(
                    "UPDATE outbox_talk_rejected SET status = 'PROCESSED' WHERE id IN (:id)"
                )
                .bindList("id", outboxRecords.stream().map(m -> (Long) m.get("id")).toList())
                .execute();
        });
    }
}
