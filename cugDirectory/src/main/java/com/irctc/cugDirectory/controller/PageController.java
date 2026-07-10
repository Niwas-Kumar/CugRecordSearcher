package com.irctc.cugDirectory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String search() {
        return "search";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change_password";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin_users";
    }

    @GetMapping("/admin/records")
    public String adminRecords() {
        return "admin_records";
    }
}