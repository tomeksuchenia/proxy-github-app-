package com.proxy;


import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = ProxyGithubApplication.class)
@AutoConfigureMockMvc
class GithubControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnReposForUserWhenGithubReturnsCorrectData() throws Exception {

        //GIVEN
        String expectedRepoName = "testRepo";
        String username = "testUser";
        GithubClientStub.stubGithubCallSuccess(wireMock, username, false);

        //WHEN THEN
        mockMvc.perform(get("/api/v1/github/users/" + username + "/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(expectedRepoName))
                .andExpect(jsonPath("$[0].owner").value(username))
                .andExpect(jsonPath("$[0].branches").isArray())
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].sha").value("abc123sha"))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void shouldReturnEmptyListWhenUserHasReposButItsFork() throws Exception {

        //GIVEN
        String username = "testUser";
        GithubClientStub.stubGithubCallSuccess(wireMock, username, true);

        // WHEN THEN
        mockMvc.perform(get("/api/v1/github/users/" + username + "/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnEmptyListWhenGithubUserExistsButHasNoRepos() throws Exception {

        //GIVEN
        String username = "emptyUser";
        GithubClientStub.stubGithubCallSuccessUserHasNoRepos(wireMock, username);

        // WHEN THEN
        mockMvc.perform(get("/api/v1/github/users/" + username + "/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn404WhenUserNotFoundOnGithub() throws Exception {
        // GIVEN
        String username = "nonExistentUser";
        GithubClientStub.stubGithubCallUserNotFound(wireMock, username);

        // WHEN THEN
        mockMvc.perform(get("/api/v1/github/users/" + username + "/repos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}