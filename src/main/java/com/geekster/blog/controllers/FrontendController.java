package com.geekster.blog.controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/me")
    public String profile() {
        return "profile";
    }

    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable long postId, Model model) {
        model.addAttribute("postId", postId);
        return "post-detail";
    }
}
