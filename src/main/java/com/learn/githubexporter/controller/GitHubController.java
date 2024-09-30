package com.learn.githubexporter.controller;

import com.learn.githubexporter.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubController {

  private final GitHubService gitHubService;

  @Autowired
  public GitHubController(GitHubService gitHubService) {
    this.gitHubService = gitHubService;
  }

  /**
   * Endpoint to fetch the last commit time for all repositories of a specified
   * GitHub user.
   *
   * @param username The GitHub username.
   * @return A ResponseEntity with the status of the operation.
   */
  @GetMapping("/export/{username}")
  public ResponseEntity<String> exportData(@PathVariable String username) {
    try {
      gitHubService.fetchRepositories(username);
      return ResponseEntity.ok("Metrics for user '" + username + "' have been updated.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while processing the request: " + e.getMessage());
    }
  }
}
