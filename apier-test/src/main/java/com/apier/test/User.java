package com.apier.test;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class User {

    @JsonView(UserView.Read.class)
    private String id;

    @JsonView(UserView.Read.class)
    private String name;

    @JsonView()
    private ZonedDateTime createdAt;
}
