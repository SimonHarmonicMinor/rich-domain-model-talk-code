package com.example.demo.domain.service;

import com.example.demo.domain.Speaker;
import com.example.demo.domain.Talk;
import com.example.demo.repo.SpeakerRepository;
import com.example.demo.repo.TalkRepository;
import com.example.demo.service.CannotSubmitTalkException;
import com.example.demo.service.TalkSubmittedResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TalkService {
    private final SpeakerRepository speakerRepository;
    private final TalkRepository talkRepository;

    @Transactional
    public TalkSubmittedResult submitTalk(Long speakerId, String title) {
        var speaker = speakerRepository.findByIdForUpdate(speakerId).orElseThrow();
        var acceptedTalksCount = talkRepository.countByStatus(Talk.Status.ACCEPTED);
        // whether speaker is experienced
        var experienced = acceptedTalksCount >= 10;
        var maxSubmittedTalksCount = experienced ? 3 : 5;
        var submittedTalksCount = talkRepository.countByStatus(Talk.Status.SUBMITTED);
        if (submittedTalksCount >= maxSubmittedTalksCount) {
            throw new CannotSubmitTalkException("Submitted talks count is maximum: " + maxSubmittedTalksCount);
        }
        var talk = new Talk();
        talk.setSpeaker(speaker);
        talk.setStatus(Talk.Status.SUBMITTED);
        talk.setTitle(title);
        return new TalkSubmittedResult(
            talkRepository.saveAndFlush(talk)
                .getId()
        );
    }
}
