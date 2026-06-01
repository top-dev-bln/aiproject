package com.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
@Tag(name = "test-controller", description = "Public/user/admin access test endpoints and Kafka interceptor demo")
public class TestController {
  @GetMapping("/all")
  @Operation(summary = "Public access test", description = "Open endpoint to verify anonymous access works")
  public String allAccess() {
    return "Public Content.";
  }

  @GetMapping("/user")
  @Operation(summary = "User access test", description = "Requires USER or ADMIN role")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public String userAccess() {
    return "User Content.";
  }

  @GetMapping("/admin")
  @Operation(summary = "Admin access test", description = "Requires ADMIN role")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminAccess() {
    return "Admin Board.";
  }

  @PostMapping("/kafka-test")
  @Operation(summary = "Kafka interceptor demo", description = "Produces/consumes a message to trigger KafkaRecordInterceptor")
  public String testKafkaInterceptor() {
    return "Kafka test endpoint - this will trigger the interceptor when messages are consumed";
  }
}
