package com.example.demo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.domain.Talk.Status;
import com.example.demo.exception.CannotAcceptTalkException;
import com.example.demo.exception.CannotRejectTalkException;
import org.junit.jupiter.api.Test;

class SpeakerTest {

  @Test
  void shouldSubmitAndAcceptTalkSuccessfully() {
    Speaker speaker = Speaker.newSpeaker("Michael", "Green");

    int talkNumber = assertDoesNotThrow(() -> speaker.submitTalk("Hibernate vs JOOQ"));

    assertDoesNotThrow(() -> speaker.acceptTalk(talkNumber));

    assertEquals(
        1,
        speaker.getTalks().size()
    );
    Talk talk = speaker.getTalks().get(0);
    assertEquals("Hibernate vs JOOQ", talk.getTitle());
    assertEquals(talkNumber, talk.getNumber());
    assertEquals(Status.ACCEPTED, talk.getStatus());
  }

  @Test
  void shouldThrowExceptionIfCannotAcceptTalkTwice() {
    Speaker speaker = Speaker.newSpeaker("Julia", "Purple");

    int talkNumber = assertDoesNotThrow(() -> speaker.submitTalk("Spring vs Micronaut"));
    assertDoesNotThrow(() -> speaker.acceptTalk(talkNumber));

    assertThrows(CannotAcceptTalkException.class, () -> speaker.acceptTalk(talkNumber));
  }

  @Test
  void shouldThrowExceptionIfCannotRejectTalkItItWasAccepted() {
    Speaker speaker = Speaker.newSpeaker("Richard", "Yellow");

    int talkNumber = assertDoesNotThrow(() -> speaker.submitTalk("Kafka vs RabbitMQ"));
    assertDoesNotThrow(() -> speaker.acceptTalk(talkNumber));

    assertThrows(CannotRejectTalkException.class, () -> speaker.rejectTalk(talkNumber));
  }

  @Test
  void shouldRegisterEventOnTalkRejection() {
    Speaker speaker = Speaker.newSpeaker("Henry", "White");

    int talkNumber = assertDoesNotThrow(() -> speaker.submitTalk("Kafka vs RabbitMQ"));
    assertDoesNotThrow(() -> speaker.rejectTalk(talkNumber));

    assertThat(
        speaker.domainEvents(),
        contains(new TalkRejectedEvent(speaker.getId(), talkNumber))
    );
  }
}