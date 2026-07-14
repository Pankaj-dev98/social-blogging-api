package com.geekster.blog.controllers;

import com.geekster.blog.domainmodel.User;
import com.geekster.blog.service.UserService;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.SignInDto;
import com.geekster.blog.uitls.dto.SignUpDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // signup
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody @Valid SignUpDto signUpDto) {
        return userService.signUp(signUpDto);
    }

    // signin
    @PostMapping("/signin")
    public String singIn(@RequestBody SignInDto signInDto) {
        return userService.signin(signInDto);
    }
    // signout
    @DeleteMapping("/signout")
    public String signOut(@RequestParam String email, @RequestParam String token) {
        return userService.signOut(new AuthDto(email, token));
    }

    // follow another user
    @PutMapping("/follow/{userId}")
    public String followUser(@RequestBody AuthDto authDto, @PathVariable long userId) {
        return userService.followUser(authDto, userId);
    }

    // Unfollow another user
    @DeleteMapping("/follow/{userToUnfollowId}")
    public String unfollowUser(@RequestParam String email, @RequestParam String token, Long userToUnfollowId) {
        return userService.unfollowUser(new AuthDto(email, token), userToUnfollowId);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, List<String>>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/{userId}")
    public Map<String, List<String>> getUserById(@PathVariable long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/followers")
    public List<String> getFollowers(@RequestParam String email, String token) {
        return userService.getFollowers(new AuthDto(email, token));
    }
}