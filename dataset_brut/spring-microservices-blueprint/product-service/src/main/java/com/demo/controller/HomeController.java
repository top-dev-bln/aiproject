package com.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.stereotype.Controller;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Controller
@Hidden
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";
    }

}
