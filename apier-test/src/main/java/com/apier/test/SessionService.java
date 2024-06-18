package com.apier.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Session create(final Session session) {
        sessionRepository.save(session);
        return session;
    }

    public Session createWithPart(final Session session) {
        return create(session);
    }

    public List<Session> findAll(final SessionCriteria session) {
        return sessionRepository.findAll();
    }
}
