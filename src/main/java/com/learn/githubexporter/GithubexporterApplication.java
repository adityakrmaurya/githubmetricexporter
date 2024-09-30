package com.learn.githubexporter;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GithubexporterApplication {
  @Value("${github.token}")
  private String githubToken;

  public static void main(String[] args) {
    SpringApplication.run(GithubexporterApplication.class, args);
  }

  /**
   * Creates a RestTemplate bean.
   *
   * @return a RestTemplate instance
   */
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    // Add an interceptor to add headers for the token
    restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
      request.getHeaders().add("Authorization", "Bearer " + githubToken);
      request.getHeaders().add("Accept", "application/vnd.github+json");
      return execution.execute(request, body);
    }));

    return restTemplate;
  }
}
