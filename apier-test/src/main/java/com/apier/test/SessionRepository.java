package com.apier.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends QuerydslPredicateExecutor<Session>, JpaRepository<Session, Long> {
}
