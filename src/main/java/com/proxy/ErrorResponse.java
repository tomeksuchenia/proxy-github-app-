package com.proxy;

public record ErrorResponse(
        int status,
        String message
) {}
