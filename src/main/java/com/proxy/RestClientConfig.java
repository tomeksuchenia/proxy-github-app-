package com.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class RestClientConfig {

    @Value("${github.api.url}")
    private String githubApiUrl;
    @Value("${github.api.token}")
    private String githubToken;

    @Bean
    GithubClient githubClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(githubApiUrl)
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultStatusHandler(
                        status -> status == HttpStatus.NOT_FOUND,
                        (request, response) -> {
                            String path = request.getURI().getPath();
                            if (path.startsWith("/users/")) {
                                throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "User not found"
                                );
                            }
                        }
                )
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(GithubClient.class);
    }
}
