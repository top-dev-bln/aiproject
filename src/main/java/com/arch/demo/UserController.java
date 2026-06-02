package com.arch.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User getUser(Long id) {
        return userService.findById(id);
    }

    public User createUser(User user) {
        return userService.save(user);
    }

    public void deleteUser(Long id) {
        userService.delete(id);
    }
}
