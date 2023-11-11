package com.example.demo.repo;

import com.example.demo.domain.Speaker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {
    @Query("FROM Speaker s WHERE s.id = :id")
    @Lock(PESSIMISTIC_WRITE)
    Optional<Speaker> findByIdForUpdate(Long id);
}
