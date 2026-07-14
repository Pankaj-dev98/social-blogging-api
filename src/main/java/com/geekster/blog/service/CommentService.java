package com.geekster.blog.service;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.domainmodel.Post;
import com.geekster.blog.domainmodel.User;
import com.geekster.blog.persistence.CommentDAO;
import com.geekster.blog.persistence.PostDao;
import com.geekster.blog.persistence.UserDAO;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.CommentUpdationDto;
import com.geekster.blog.uitls.dto.NewCommentDto;
import com.geekster.blog.uitls.exceptions.EmptyResultListException;
import com.geekster.blog.uitls.exceptions.InvalidCredentialsException;
import com.geekster.blog.uitls.exceptions.PageIndexOutOfBoundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    private CommentDAO commentDAO;
    private UserService userService;

    private UserDAO userDAO;
    private PostDao postDAO;

    @Autowired
    public CommentService(CommentDAO commentDAO, UserDAO userDAO, PostDao postDAO) {
        this.commentDAO = commentDAO;
        this.userDAO = userDAO;
        this.postDAO = postDAO;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String addComment(NewCommentDto commentDto) {
        if(!userService.authenticateUser(commentDto.getAuthDto()))
            throw new InvalidCredentialsException("Invalid credentials entered. Please try again");

        User user = userDAO.findByEmail(commentDto.getAuthDto().getEmail());

        String comment = commentDto.getCommentText();
        long postId = commentDto.getPostId();

        Post post = postDAO.findById(postId).orElseThrow();
        List<Comment> comments = post.getComments();

        if(comments == null)
            post.setComments(new LinkedList<>());

        post.getComments().add(new Comment(0, comment, LocalDateTime.now(), user));
        postDAO.save(post);

        return String.format("New Comment added by %s %n %s", user.getName(), comment);
    }

    public String test() {
        User user = new User();
        user.setId(1);

        return commentDAO.findByUser(user).toString();
    }

    public List<Comment> getCommentsByUserId(long userId, int page, int size) {
        User user = userDAO.findById(userId).orElseThrow();

        if(user.getUserComments() == null)
            throw new EmptyResultListException("User has not posted any comments");

        return getPaginatedSubList(user.getUserComments(), page, size);
    }

    public ResponseEntity<Map<Long, List<String>>> getCommentsByPostId(long postId) {
        Post post = postDAO.findById(postId).orElseThrow();

        if(post.getComments() == null || post.getComments().size() == 0)
            throw new EmptyResultListException("Post has no comments yet.");

        return convertToResponse(post.getComments());
    }

    private static ResponseEntity<Map<Long, List<String>>> convertToResponse(List<Comment> comments) {
        Map<Long, List<String>> map = new HashMap<>();

        comments.forEach(comment -> {
           map.put(comment.getCommentId(), List.of(
                   "Comment: " + comment.getCommentText(), "By user: " + comment.getUser().getName() + '{' + comment.getUser().getEmail() + '}' ));
        });
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    private static List<Comment> getPaginatedSubList(List<Comment> list, int page, int size) {
        int start = page*size;

        if(start >= list.size())
            throw new PageIndexOutOfBoundsException("Max pages index for size " + size + " is "
                    + (list.size()/size) + "[0-indexed]" );

        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    public String updateComment(CommentUpdationDto commentUpdationDto) {
        if(!userService.authenticateUser(commentUpdationDto.getAuthDto()))
            throw new InvalidCredentialsException("Invalid credentials. Could not update comment");

        User user = userDAO.findByEmail(commentUpdationDto.getAuthDto().getEmail());

        List<Comment> comments = user.getUserComments();

        for(Comment c: comments) {
            if(c.getCommentId() == commentUpdationDto.getCommentId()) {
                c.setCommentText(commentUpdationDto.getUpdatedComment());
                userDAO.save(user);
                return "Comment successfully updated by " + user.getName() + " for Comment #" + c.getCommentId();
            }
        }
        throw new InvalidCredentialsException("Invalid comment Id provided");
    }

    public String deleteComment(long commentId, AuthDto authDto) {
        if(!userService.authenticateUser(authDto))
            throw new InvalidCredentialsException("Invalid credentials. Could not update comment");

        User user = userDAO.findByEmail(authDto.getEmail());

        Comment comment = commentDAO.findByCommentIdAndUser(commentId, user);
        if(comment == null)
            return "User is not the owner of provided Comment";

        commentDAO.delete(comment);
        return "Removed comment successfully";
    }
}
