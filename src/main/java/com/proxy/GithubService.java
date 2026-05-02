package com.proxy;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepoModel> fetchAllRepos(final String username) {
        var repos = Optional.ofNullable(githubClient.fetchUserRepos(username))
                .orElse(List.of());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            var tasks = repos
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(r -> Boolean.FALSE.equals(r.fork()))
                    .map(repo -> executor.submit(() -> fetchBranchesAndMap(username, repo)))
                    .toList();
            return tasks
                    .stream()
                    .map(this::getFutureResult)
                    .toList();
        }
    }

    private RepoModel fetchBranchesAndMap(String username, RepoModel repo) {
        var branches = githubClient.fetchBranches(username, repo.name());
        return new RepoModel(
                repo.name(),
                repo.owner(),
                branches,
                repo.fork());
    }

    private RepoModel getFutureResult(Future<RepoModel> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch repo details", e);
        }
    }
}
