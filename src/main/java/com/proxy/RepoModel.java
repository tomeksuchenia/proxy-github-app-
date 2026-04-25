package com.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

record RepoModel(
        String name,
        String owner,
        List<BranchModel> branches,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        boolean fork
) {
    RepoModel(
            @JsonProperty("name") String name,
            @JsonProperty("owner") Map<String, Object> owner,
            @JsonProperty("fork") boolean fork
    ) {
        this(
                name,
                owner != null ? (String) owner.get("login") : null,
                null,
                fork
        );
    }
}


