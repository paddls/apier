package com.apier.test;

import com.apier.core.ResourceApi;
import com.apier.core.ResourceController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

@ResourceController(value = "/sessions", service = SessionService.class)
public interface SessionController {
    @ResourceApi
    Session create(@RequestBody final Session session);

    @ResourceApi
    Session createWithPart(@RequestPart("body") final Session session);

    @ResourceApi
    List<Session> findAll(final SessionCriteria criteria);

}
