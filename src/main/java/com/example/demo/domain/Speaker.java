    package com.example.demo.domain;

import com.example.demo.domain.Talk.Status;
import com.example.demo.exception.CannotAcceptTalkException;
import com.example.demo.exception.CannotRejectTalkException;
import com.example.demo.exception.CannotSubmitTalkException;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Speaker extends AggregateRoot<Speaker> {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @OneToMany(mappedBy = "speaker")
    private List<Talk> talks = new ArrayList<>();

    public static Speaker newSpeaker(String firstName, String lastName) {
        Speaker speaker = new Speaker();
        speaker.firstName = firstName;
        speaker.lastName = lastName;
        return speaker;
    }

    public int submitTalk(String title) {
        boolean experienced = countTalksByStatus(Status.ACCEPTED) >= 10;
        int maxSubmittedTalksCount = experienced ? 3 : 5;
        if (countTalksByStatus(Status.SUBMITTED) >= maxSubmittedTalksCount) {
            throw new CannotSubmitTalkException("Submitted talks count is maximum: " + maxSubmittedTalksCount);
        }
        Talk talk = Talk.newTalk(this, Status.SUBMITTED, title, talks.size() + 1);
        talks.add(talk);
        return talk.getNumber();
    }

    private long countTalksByStatus(Talk.Status status) {
        return talks.stream().filter(t -> t.getStatus().equals(status)).count();
    }

    public void acceptTalk(int talkNumber) {
        Talk talk = talkByNumber(talkNumber);
        talk.setStatus(status -> {
            if (!status.equals(Status.SUBMITTED)) {
                throw new CannotAcceptTalkException("");
            }
            return Status.ACCEPTED;
        });
    }

    public void rejectTalk(int talkNumber) {
        Talk talk = talkByNumber(talkNumber);
        talk.setStatus(status -> {
            if (!status.equals(Status.SUBMITTED)) {
                throw new CannotRejectTalkException("");
            }
            return Status.REJECTED;
        });
        registerEvent(new TalkRejectedEvent(this.id, talkNumber));
    }

    private Talk talkByNumber(int number) {
        return talks.stream().filter(t -> Objects.equals(t.getNumber(), number)).findFirst().orElseThrow();
    }
}
