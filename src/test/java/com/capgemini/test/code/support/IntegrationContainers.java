package com.capgemini.test.code.support;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.utility.MountableFile;

/**
 * Postgres and MockServer shared by every integration test in the run. MockServer loads the same
 * expectations as docker-compose, so tests run against the provided contract.
 */
public abstract class IntegrationContainers {

  protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

  protected static final MockServerContainer MOCK_SERVER =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"))
          .withCopyFileToContainer(MountableFile.forHostPath("docker/mockserver-config/init.json"),
              "/config/init.json")
          .withEnv("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/init.json");

  static {
    Startables.deepStart(POSTGRES, MOCK_SERVER).join();
  }

  @DynamicPropertySource
  static void containerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("external.service.url", MOCK_SERVER::getEndpoint);
  }

  /** Fails unless MockServer received exactly one POST to {@code path} whose JSON body contains {@code json}. */
  protected static void assertMockServerReceivedOnce(String path, String json) {
    String verification = """
        {"httpRequest": {"method": "POST", "path": "%s", "body": {"type": "JSON", "json": %s}},
         "times": {"atLeast": 1, "atMost": 1}}
        """.formatted(path, json);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // MockServer answers 406 with the mismatch details, which RestTemplate raises as an exception.
    new RestTemplate().put(MOCK_SERVER.getEndpoint() + "/mockserver/verify", new HttpEntity<>(verification, headers));
  }
}
