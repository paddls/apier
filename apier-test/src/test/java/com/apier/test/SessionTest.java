package com.apier.test;


import org.junit.Test;

public class SessionTest {

    @Test
    public void testSessionCriteria() {
        final SessionCriteria criteria = new SessionCriteria();
        final SessionCriteria.CoachingCriteria coachingCriteria = new SessionCriteria.CoachingCriteria();

        criteria.setCoaching(coachingCriteria);
    }
}
