package com.apier.test;

import com.apier.core.ResourceApi;
import com.apier.core.ResourceController;

import java.util.List;

@ResourceController(value = "/users", service = UserService.class)
public interface UserController {

    @ResourceApi
    List<User> findAll(final UserCriteria criteria);

}
