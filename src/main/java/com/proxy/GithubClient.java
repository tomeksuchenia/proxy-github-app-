package com.proxy;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

interface GithubClient {

    @GetExchange("users/{username}/repos")
    List<RepoModel> fetchUserRepos(@PathVariable String username);

    @GetExchange("repos/{username}/{repoName}/branches")
    List<BranchModel> fetchBranches(
            @PathVariable String username,
            @PathVariable String repoName);
}
