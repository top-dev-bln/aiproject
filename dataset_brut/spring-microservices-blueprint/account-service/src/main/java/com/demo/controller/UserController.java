package com.demo.controller;

import com.demo.payload.response.UserProfile;
import com.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@Tag(name = "user-controller", description = "User profile retrieval and current user info")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID", description = "Fetch user profile including roles by numeric identifier")
    public UserProfile getUserBy(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Return profile of the authenticated user")
    @PreAuthorize("isAuthenticated()")
    public UserProfile getMe() {
        return userService.getCurrentUserProfile();
    }

}
