package com.learn.githubexporter.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GitHubService {

  private final RestTemplate restTemplate;
  private final MeterRegistry meterRegistry;
  private final ObjectMapper objectMapper;

  @Value("${github.api.url}")
  private String githubApiUrl;

  @Value("${github.token}")
  private String githubToken;

  // Logger instance
  private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

  @Autowired
  public GitHubService(RestTemplate restTemplate, MeterRegistry meterRegistry) {
    this.restTemplate = restTemplate;
    this.meterRegistry = meterRegistry;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Fetches all repositories for a given GitHub username and records the
   * epoch time of the last commit for each repository in Prometheus.
   *
   * @param username The GitHub username.
   */
  public void fetchRepositories(String username) {
    String url = githubApiUrl.replace("{username}", username);
    logger.info("Fetching repositories for user: {}", username); // Log the URL
    String response = restTemplate.getForObject(url, String.class);

    if (response != null) {
      logger.info("Response received from GitHub API: {}", response); // Log the response
      processRepositories(response);
    } else {
      logger.warn("No response received from GitHub API for user: {}", username); // Log if response is null
    }
  }

  /**
   * Processes the repository data and registers the epoch time of the last commit
   * for each repository as a Prometheus gauge.
   *
   * @param jsonResponse The JSON response from the API.
   */
  private void processRepositories(String jsonResponse) {
    try {
      JsonNode rootNode = objectMapper.readTree(jsonResponse);
      for (JsonNode repoNode : rootNode) {
        String repoName = repoNode.get("name").asText();
        JsonNode branchesUrlNode = repoNode.get("branches_url");
        String branchesUrl = branchesUrlNode.asText().replace("{/branch}", "");

        // Fetch branches for the current repository
        logger.info("Fetching branches from: {}", branchesUrl);
        String branchesResponse = restTemplate.getForObject(branchesUrl, String.class);
        logger.info("Response received for branches: {}", branchesResponse); // Log the response for branches

        JsonNode branchesNode = objectMapper.readTree(branchesResponse);
        for (JsonNode branchNode : branchesNode) {
          String branchName = branchNode.get("name").asText();
          JsonNode commitNode = branchNode.get("commit");
          String commitSha = commitNode.get("sha").asText();
          String commitUrl = commitNode.get("url").asText();
          logger.info("Fetching commit details from: {}", commitUrl);
          String commitResponse = restTemplate.getForObject(commitUrl, String.class);
          logger.info("Response received for commit: {}", commitResponse); // Log the commit response

          JsonNode commitDetailNode = objectMapper.readTree(commitResponse);
          long lastCommitEpochTime = Instant.parse(commitDetailNode.get("commit").get("committer").get("date").asText())
              .toEpochMilli();

          // Register the epoch time of the last commit as a Prometheus gauge
          Gauge.builder("github_last_commit_time_epoch", () -> lastCommitEpochTime) // Use a Supplier
              .tags("repository", repoName, "branch", branchName, "commit_sha", commitSha)
              .description("Epoch time of the last commit for repository " + repoName + " on branch " + branchName)
              .register(meterRegistry);

          logger.info("Registered last commit time for repository: {} on branch: {} with epoch time: {}", repoName,
              branchName, lastCommitEpochTime);
        }
      }
    } catch (Exception e) {
      logger.error("Error processing repository data: {}", e.getMessage(), e); // Log exceptions
    }
  }

}
