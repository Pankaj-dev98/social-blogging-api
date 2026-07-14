package com.geekster.blog.service;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.domainmodel.Post;
import com.geekster.blog.domainmodel.User;
import com.geekster.blog.persistence.PostDao;
import com.geekster.blog.persistence.UserDAO;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.NewPostDto;
import com.geekster.blog.uitls.dto.PostUpdationDto;
import com.geekster.blog.uitls.exceptions.EmptyResultListException;
import com.geekster.blog.uitls.exceptions.InvalidCredentialsException;
import com.geekster.blog.uitls.exceptions.PageIndexOutOfBoundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PostService {
    private PostDao postDao;
    private UserService userService;
    private UserDAO userDAO;

    public PostService(PostDao postDao, UserDAO userDAO) {
        this.postDao = postDao;
        this.userDAO = userDAO;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<Map<String, String>> addNewPost(NewPostDto postDto) {
        if(!userService.authenticateUser(postDto.getAuthDto()))
            throw new InvalidCredentialsException("Invalid credentials passed. Could not add comment \n" + postDto.getTitle());

        User user = userDAO.findByEmail(postDto.getAuthDto().getEmail());

        if(user.getPosts() == null)
            user.setPosts(new ArrayList<>());

        user.getPosts().add(new Post(0, postDto.getTitle(), postDto.getBlogText(), LocalDateTime.now(), user, new ArrayList<>()));
        userDAO.save(user);

        return convertToResponse(user, postDto.getTitle(), postDto.getBlogText());
    }

    private static ResponseEntity<Map<String, String>> convertToResponse(User user, String title, String text) {
        Map<String, String> map = new HashMap<>();

        map.put("New Post Added By", user.getName());
        map.put("Title", title);
        map.put("Content:", text);

        return new ResponseEntity<>(map, HttpStatus.ACCEPTED);
    }

    private static ResponseEntity<Map<String, String>> convertToResponse(User user, Post post) {
        Map<String, String> map = new HashMap<>();

        map.put("Post successfully updated by", user.getName());
        map.put("Post id", String.valueOf(post.getId()));
        map.put("Updated post title: ", post.getTitle());
        map.put("Updated post content: ", post.getPostText());

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public List<Post> getAllPosts(int page, int size) {
        List<Post> posts = postDao.findAll();

        int start = page*size;

        if(start >= posts.size())
            throw new PageIndexOutOfBoundsException("Max pages index for size " + size + " is "
                    + (posts.size()/size) + "[0-indexed]" );

        int end = Math.min(start + size, posts.size());

        return posts.subList(start, end);
    }

    public List<Post> getPostsByUserId(long userId) {
        User user = userDAO.findById(userId).orElseThrow();

        return user.getPosts();
    }

    public List<Post> getPostsByPhrase(String phrase) {
        List<Post> posts = postDao.findByTitleContaining(phrase);
        if(posts == null || posts.size() == 0)
            throw new EmptyResultListException("No items match the search criteria");

        return posts;
    }

    public ResponseEntity<Map<String, String>> updatePost(PostUpdationDto postUpdationDto) {
        if(!userService.authenticateUser(postUpdationDto.getAuthDto()))
            throw new InvalidCredentialsException("Invalid credentials passed. Could not update post. Please try again.");

        User user = userDAO.findByEmail(postUpdationDto.getAuthDto().getEmail());

        List<Post> posts = user.getPosts();

        for(Post post: posts) {
            if(post.getId() == postUpdationDto.getPostId()) {
                post.setTitle(postUpdationDto.getTitle());
                post.setPostText(postUpdationDto.getBlogText());
                userDAO.save(user);

                return convertToResponse(user, post);
            }
        }
        throw new EmptyResultListException("User has no post that match the search criteria");
    }

    public String deletePost(AuthDto authDto, long postId) {
        if(!userService.authenticateUser(authDto))
            throw new InvalidCredentialsException("Invalid credentials passed. Could not delete post. Please try again.");

        User user = userDAO.findByEmail(authDto.getEmail());

        List<Post> posts = user.getPosts();

        for(Post post: posts) {
            if(post.getId() == postId) {
                post.setUser(null);
                user.getPosts().remove(post);
                userDAO.save(user);
                return "Post deleted";
            }
        }

        return "Invalid post id";
    }
}
