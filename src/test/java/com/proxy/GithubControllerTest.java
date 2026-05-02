package com.proxy;


import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GithubControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ObjectMapper mapper;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", () -> "http://localhost:" + wireMock.getPort());
    }

    @Test
    void shouldReturnReposForUserWhenGithubReturnsCorrectData() {

        // GIVEN
        String expectedRepoName = "testRepo";
        String username = "testUser";

        GithubClientStub.stubGithubCallSuccess(wireMock, username, false);

        // WHEN + THEN
        restTestClient.get()
                .uri("/api/v1/github/users/{username}/repos", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo(expectedRepoName)
                .jsonPath("$[0].owner").isEqualTo(username)
                .jsonPath("$[0].branches").isArray()
                .jsonPath("$[0].branches[0].name").isEqualTo("main")
                .jsonPath("$[0].branches[0].sha").isEqualTo("abc123sha");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasReposButItsFork() {

        // GIVEN
        String username = "testUser";
        GithubClientStub.stubGithubCallSuccess(wireMock, username, true);

        // WHEN + THEN
        restTestClient.get()
                .uri("/api/v1/github/users/{username}/repos", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldReturnEmptyListWhenGithubUserExistsButHasNoRepos() {

        // GIVEN
        String username = "emptyUser";
        GithubClientStub.stubGithubCallSuccessUserHasNoRepos(wireMock, username);

        // WHEN + THEN
        restTestClient.get()
                .uri("/api/v1/github/users/{username}/repos", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldReturn404WhenUserNotFoundOnGithub() {

        // GIVEN
        String username = "nonExistentUser";
        GithubClientStub.stubGithubCallUserNotFound(wireMock, username);

        // WHEN + THEN
        restTestClient.get()
                .uri("/api/v1/github/users/{username}/repos", username)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").exists()
                .jsonPath("$.message").isEqualTo("User not found");
    }
}