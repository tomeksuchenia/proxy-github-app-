package com.proxy;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepoModel> fetchAllRepos(final String username) {
        var repos = Optional.ofNullable(githubClient.fetchUserRepos(username))
                .orElse(List.of());

        return repos.parallelStream()
                .filter(Objects::nonNull)
                .filter(r -> Boolean.FALSE.equals(r.fork()))
                .map(repo -> fetchBranchesAndMap(username, repo))
                .toList();
    }

    private RepoModel fetchBranchesAndMap(String username, RepoModel repo) {
        var branches = githubClient.fetchBranches(username, repo.name());
        return new RepoModel(
                repo.name(),
                repo.owner(),
                branches,
                repo.fork());
    }
}
