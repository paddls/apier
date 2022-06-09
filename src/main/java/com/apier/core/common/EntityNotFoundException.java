package com.apier.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityNotFoundException extends RuntimeException {
    @Getter
    private final Class<?> entity;
}
