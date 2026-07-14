package com.geekster.blog.controllers;

import com.geekster.blog.domainmodel.Post;
import com.geekster.blog.service.PostService;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.NewPostDto;
import com.geekster.blog.uitls.dto.PostUpdationDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
    private PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> addNewPost(@RequestBody @Valid NewPostDto postDto) {
        return postService.addNewPost(postDto);
    }

    @GetMapping("/")
    public List<Post> getAllPosts(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return postService.getAllPosts(page, size);
    }

    @GetMapping("/{userId}")
    public List<Post> getPostsByUserId(@PathVariable long userId) {
        return postService.getPostsByUserId(userId);
    }

    @GetMapping("/search")
    public List<Post> getPostsByPhrase(@RequestParam String phrase) {
        return postService.getPostsByPhrase(phrase);
    }

    @PutMapping("/")
    public ResponseEntity<Map<String, String>> updatePost(@RequestBody @Valid PostUpdationDto postUpdationDto) {
        return postService.updatePost(postUpdationDto);
    }

    @DeleteMapping("/")
    public String deletePost(@RequestBody AuthDto authDto, @RequestParam long postId) {
        return postService.deletePost(authDto, postId);
    }
}
