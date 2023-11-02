package com.apier.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OperationEnum {
    LIST("LIST", null, "List", "List", "findAll", "Get", false),
    DETAIL("DETAIL", null, "Detail", "Detail", "findById", "Get", true),
    CREATE("CREATE", "Create", "Create", "Detail", "create", "Post", false),
    UPDATE("UPDATE", "Update", "Update", "Detail", "update", "Put", true),
    PATCH("PATCH", "Patch", "Patch", "Detail", "patch", "Patch", true),
    DELETE("DELETE", null, "Delete", null, "delete", "Delete", true);

    @Getter
    private final String permission;

    @Getter
    private final String inputJsonView;

    @Getter
    private final String jsonView;

    @Getter
    private final String outputJsonView;

    @Getter
    private final String methodName;

    @Getter
    private final String httpMethod;

    @Getter
    private final boolean isIdMethod;
}
