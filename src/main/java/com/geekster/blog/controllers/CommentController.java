package com.geekster.blog.controllers;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.service.CommentService;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.CommentUpdationDto;
import com.geekster.blog.uitls.dto.NewCommentDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/")
    public String addComment(@RequestBody @Valid NewCommentDto commentDto) {
        return commentService.addComment(commentDto);
    }

    @GetMapping("/{userId}")
    public List<Comment> getCommentsByUserId(@PathVariable long userId,
                                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return commentService.getCommentsByUserId(userId, page, size);
    }

    @GetMapping("/")
    public ResponseEntity<Map<Long, List<String>>> getCommentsByPostId(@RequestParam long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @PutMapping("/")
    public String updateComment(@RequestBody @Valid CommentUpdationDto commentUpdationDto) {
        return commentService.updateComment(commentUpdationDto);
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable long commentId, @RequestBody AuthDto authDto) {
        return commentService.deleteComment(commentId, authDto);
    }
}
