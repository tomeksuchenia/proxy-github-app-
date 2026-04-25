package com.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

record BranchModel(
        String name,
        String sha
) {
    BranchModel(
            @JsonProperty("name") String name,
            @JsonProperty("commit") Map<String, Object> commit
    ) {
        this(
                name,
                commit != null ? (String) commit.get("sha") : null
        );
    }
}
