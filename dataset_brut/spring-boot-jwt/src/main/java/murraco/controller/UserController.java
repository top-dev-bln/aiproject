package murraco.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import murraco.model.AppUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import murraco.dto.UserDataDTO;
import murraco.dto.UserResponseDTO;
import murraco.service.UserService;

@RestController
@RequestMapping("/users")
@Tag(name = "users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ModelMapper modelMapper;

  @PostMapping("/signin")
  @Operation(summary = "Authenticates user and returns a JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "422", description = "Invalid username/password supplied")})
  public String login(
      @Parameter(description = "Username") @RequestParam String username,
      @Parameter(description = "Password") @RequestParam String password) {
    return userService.signin(username, password);
  }

  @PostMapping("/signup")
  @Operation(summary = "Creates user and returns a JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "422", description = "Username is already in use")})
  public String signup(@Parameter(description = "Signup User") @RequestBody @Valid UserDataDTO user) {
    return userService.signup(modelMapper.map(user, AppUser.class));
  }

  @DeleteMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "Deletes user by username")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public String delete(@Parameter(description = "Username") @PathVariable String username) {
    userService.delete(username);
    return username;
  }

  @GetMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "Returns user by username")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public UserResponseDTO search(@Parameter(description = "Username") @PathVariable String username) {
    return modelMapper.map(userService.search(username), UserResponseDTO.class);
  }

  @GetMapping(value = "/me")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  @Operation(summary = "Returns the authenticated user's data")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "400", description = "Something went wrong"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token")})
  public UserResponseDTO whoami(HttpServletRequest req) {
    return modelMapper.map(userService.whoami(req), UserResponseDTO.class);
  }

  @GetMapping("/refresh")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  @Operation(summary = "Issues a new JWT for the authenticated user")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "New JWT issued"),
      @ApiResponse(responseCode = "401", description = "Expired or invalid JWT token"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "User no longer exists")})
  public String refresh(Authentication authentication) {
    return userService.refresh(authentication.getName());
  }

}
