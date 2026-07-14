package com.geekster.blog.controllers;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.domainmodel.Post;
import com.geekster.blog.domainmodel.User;
import com.geekster.blog.persistence.PostDao;
import com.geekster.blog.persistence.UserDAO;
import com.geekster.blog.service.UserService;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.exceptions.EmptyResultListException;
import com.geekster.blog.uitls.exceptions.NoSuchUserException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/ui")
public class UiController {
    private final UserDAO userDAO;
    private final PostDao postDao;
    private final UserService userService;

    public UiController(UserDAO userDAO, PostDao postDao, UserService userService) {
        this.userDAO = userDAO;
        this.postDao = postDao;
        this.userService = userService;
    }

    @GetMapping("/me")
    public AccountSummary me(@RequestParam String email, @RequestParam String token) {
        User currentUser = authenticate(email, token);
        return toAccountSummary(currentUser, currentUser);
    }

    @GetMapping("/me/posts")
    public AccountPosts myPosts(@RequestParam String email, @RequestParam String token) {
        User currentUser = authenticate(email, token);

        List<PostPreview> posts = nullSafe(currentUser.getPosts()).stream()
                .sorted(newestFirst())
                .map(this::toPostPreview)
                .toList();

        return new AccountPosts(toAccountSummary(currentUser, currentUser), posts);
    }

    @GetMapping("/feed")
    public List<PostPreview> feed(@RequestParam String email, @RequestParam String token) {
        User currentUser = authenticate(email, token);

        return nullSafe(currentUser.getFollowing()).stream()
                .flatMap(user -> nullSafe(user.getPosts()).stream())
                .sorted(newestFirst())
                .map(this::toPostPreview)
                .toList();
    }

    @GetMapping("/followers")
    public List<AccountSummary> followers(@RequestParam String email, @RequestParam String token) {
        User currentUser = authenticate(email, token);

        return nullSafe(currentUser.getFollowers()).stream()
                .map(user -> toAccountSummary(user, currentUser))
                .toList();
    }

    @GetMapping("/accounts/search")
    public List<AccountSummary> searchAccounts(@RequestParam String email,
                                               @RequestParam String token,
                                               @RequestParam String name) {
        User currentUser = authenticate(email, token);

        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        return userDAO.findByNameContainingIgnoreCase(name.trim()).stream()
                .filter(user -> user.getId() != currentUser.getId())
                .map(user -> toAccountSummary(user, currentUser))
                .toList();
    }

    @GetMapping("/accounts/{accountId}/posts")
    public AccountPosts accountPosts(@RequestParam String email,
                                     @RequestParam String token,
                                     @PathVariable long accountId) {
        User currentUser = authenticate(email, token);
        User account = userDAO.findById(accountId)
                .orElseThrow(() -> new NoSuchUserException("User with id " + accountId + " does not exist"));

        List<PostPreview> posts = nullSafe(account.getPosts()).stream()
                .sorted(newestFirst())
                .map(this::toPostPreview)
                .toList();

        return new AccountPosts(toAccountSummary(account, currentUser), posts);
    }

    @PostMapping("/accounts/{accountId}/follow")
    public AccountSummary followAccount(@RequestBody AuthDto authDto, @PathVariable long accountId) {
        userService.followUser(authDto, accountId);
        User currentUser = authenticate(authDto.getEmail(), authDto.getToken());
        User account = userDAO.findById(accountId)
                .orElseThrow(() -> new NoSuchUserException("User with id " + accountId + " does not exist"));

        return toAccountSummary(account, currentUser);
    }

    @GetMapping("/posts/{postId}")
    public PostDetail postDetail(@RequestParam String email,
                                 @RequestParam String token,
                                 @PathVariable long postId) {
        authenticate(email, token);
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new EmptyResultListException("Post with id " + postId + " does not exist"));

        return new PostDetail(
                post.getId(),
                post.getTitle(),
                post.getPostText(),
                post.getCreationTime(),
                post.getUser() == null ? null : post.getUser().getId(),
                post.getUser() == null ? "Unknown user" : post.getUser().getName(),
                nullSafe(post.getComments()).stream()
                        .sorted(Comparator.comparing(Comment::getCommentCreationTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(comment -> new CommentView(
                                comment.getCommentId(),
                                comment.getCommentText(),
                                comment.getCommentCreationTime(),
                                comment.getUser() == null ? "Unknown user" : comment.getUser().getName()
                        ))
                        .toList()
        );
    }

    private User authenticate(String email, String token) {
        String normalizedEmail = email == null ? null : email.toLowerCase();
        AuthDto authDto = new AuthDto(normalizedEmail, token);

        if (!userService.authenticateUser(authDto)) {
            throw new NoSuchUserException("Please sign in again");
        }

        return userDAO.findByEmail(normalizedEmail);
    }

    private AccountSummary toAccountSummary(User user, User currentUser) {
        return new AccountSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                isFollowing(currentUser, user),
                nullSafe(user.getFollowers()).size(),
                nullSafe(user.getPosts()).size()
        );
    }

    private PostPreview toPostPreview(Post post) {
        return new PostPreview(
                post.getId(),
                post.getTitle(),
                post.getPostText(),
                post.getCreationTime(),
                post.getUser() == null ? null : post.getUser().getId(),
                post.getUser() == null ? "Unknown user" : post.getUser().getName(),
                nullSafe(post.getComments()).size()
        );
    }

    private boolean isFollowing(User currentUser, User account) {
        return nullSafe(currentUser.getFollowing()).stream()
                .anyMatch(user -> user.getId() == account.getId());
    }

    private Comparator<Post> newestFirst() {
        return (left, right) -> {
            LocalDateTime leftTime = left.getCreationTime();
            LocalDateTime rightTime = right.getCreationTime();

            if (leftTime == null && rightTime == null) {
                return 0;
            }
            if (leftTime == null) {
                return 1;
            }
            if (rightTime == null) {
                return -1;
            }
            return rightTime.compareTo(leftTime);
        };
    }

    private <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    public record AccountSummary(long id, String name, String email, boolean following, int followerCount, int postCount) {
    }

    public record AccountPosts(AccountSummary account, List<PostPreview> posts) {
    }

    public record PostPreview(long id, String title, String postText, LocalDateTime creationTime, Long authorId,
                              String authorName, int commentCount) {
    }

    public record PostDetail(long id, String title, String postText, LocalDateTime creationTime, Long authorId,
                             String authorName, List<CommentView> comments) {
    }

    public record CommentView(long id, String commentText, LocalDateTime creationTime, String authorName) {
    }
}
