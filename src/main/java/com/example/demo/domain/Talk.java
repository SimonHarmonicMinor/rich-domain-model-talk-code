package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
public class Talk {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "speaker_id")
    private Speaker speaker;

    @Enumerated(STRING)
    private Status status;

    private String title;

    @Column(updatable = false)
    private int number;

    public static Talk newTalk(Speaker speaker, Status status, String title, int number) {
        Talk talk = new Talk();
        talk.number = number;
        talk.speaker = speaker;
        talk.status = status;
        talk.title = title;
        return talk;
    }

    void setStatus(Function<Status, Status> fnStatus) {
        this.status = fnStatus.apply(this.status);
    }
    public enum Status {
        SUBMITTED, ACCEPTED, REJECTED

    }
}
