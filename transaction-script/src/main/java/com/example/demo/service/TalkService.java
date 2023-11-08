package com.example.demo.service;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TalkService {
    private final Jdbi jdbi;

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
}
