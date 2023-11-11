package com.example.demo.repo;

import com.example.demo.domain.Talk;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkRepository extends JpaRepository<Talk, Long> {
    long countByStatus(Talk.Status status);
}
