package com.apier.test;


import com.apier.core.criteria.BooleanCriteria;
import org.junit.Test;

public class SessionTest {

    @Test
    public void testSessionCriteria() {
        final SessionCriteria criteria = new SessionCriteria();
        final BooleanCriteria booleanCriteria = new BooleanCriteria();
        final SessionCriteria.CoachingCriteria coachingCriteria = new SessionCriteria.CoachingCriteria();

        criteria.setCoaching(coachingCriteria);
        criteria.setIsActive(booleanCriteria);
    }
}
