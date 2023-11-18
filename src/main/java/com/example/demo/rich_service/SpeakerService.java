package com.example.demo.rich_service;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

import com.example.demo.domain.Speaker;
import com.example.demo.domain.TalkRejectedEvent;
import com.example.demo.repo.SpeakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class SpeakerService {
  private final SpeakerRepository speakerRepository;

  @Transactional
  public TalkSubmittedResult submitTalk(Long speakerId, String talkTitle) {
    Speaker speaker = speakerRepository.findByIdForUpdate(speakerId).orElseThrow();
    int talkNumber = speaker.submitTalk(talkTitle);
    speakerRepository.save(speaker);
    return new TalkSubmittedResult(talkNumber);
  }

  @Transactional
  public void acceptTalk(Long speakerId, int talkNumber) {
    Speaker speaker = speakerRepository.findByIdForUpdate(speakerId).orElseThrow();
    speaker.acceptTalk(talkNumber);
    speakerRepository.save(speaker);
  }

  @Transactional
  public void rejectTalk(Long speakerId, int talkNumber) {
    Speaker speaker = speakerRepository.findByIdForUpdate(speakerId).orElseThrow();
    speaker.rejectTalk(talkNumber);
    speakerRepository.save(speaker);
  }

  @TransactionalEventListener(phase = BEFORE_COMMIT)
  public void onTalkRejected(TalkRejectedEvent event) {
    // do something...
  }
}
