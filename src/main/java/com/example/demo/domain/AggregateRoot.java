package com.example.demo.domain;

import java.util.Collection;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.DomainEvents;

public class AggregateRoot<T extends AggregateRoot<T>> extends AbstractAggregateRoot<T> {
  @Override
  @DomainEvents
  public Collection<Object> domainEvents() {
    return super.domainEvents();
  }
}
