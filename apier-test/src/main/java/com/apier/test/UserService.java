package com.apier.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    public List<User> findAll(final UserCriteria session) {
        throw new RuntimeException("Not implemented");
    }
}
