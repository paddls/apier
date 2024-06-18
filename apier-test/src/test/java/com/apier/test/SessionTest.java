package com.apier.test;


import com.apier.core.criteria.querydsl.criteria.QBooleanCriteria;
import org.junit.Test;

public class SessionTest {

    @Test
    public void testSessionCriteria() {
        final SessionCriteria criteria = new SessionCriteria();
        final QBooleanCriteria booleanCriteria = new QBooleanCriteria();
        final SessionCriteria.CoachingCriteria coachingCriteria = new SessionCriteria.CoachingCriteria();

        criteria.setCoaching(coachingCriteria);
        criteria.setIsActive(booleanCriteria);
    }
}
