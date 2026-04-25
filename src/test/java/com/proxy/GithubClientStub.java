package com.proxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class GithubClientStub {

    static void stubGithubCallSuccess(WireMockExtension wireMock, String username, boolean fork) {

        String repoName = "testRepo";

        wireMock.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "%s",
                                    "owner": { "login": "%s" },
                                    "fork": %s
                                  }
                                ]
                                """.formatted(repoName, username, fork))));

        wireMock.stubFor(get(urlEqualTo("/repos/" + username + "/" + repoName + "/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": { "sha": "abc123sha" }
                                  }
                                ]
                                """)));
    }

    static void stubGithubCallSuccessUserHasNoRepos(WireMockExtension wireMock, String username) {

        wireMock.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                ]
                                """)));
    }

    static void stubGithubCallUserNotFound(WireMockExtension wireMock, String username) {
        wireMock.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse().withStatus(404)));
    }
}
