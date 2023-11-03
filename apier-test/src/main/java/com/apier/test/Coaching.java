package com.apier.test;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
public class Coaching {

    @Id
    @JsonView(SessionView.Read.class)
    private Long id;

    @JsonView(SessionView.Read.class)
    private String name;

    @JsonView()
    private ZonedDateTime createdAt;
}
