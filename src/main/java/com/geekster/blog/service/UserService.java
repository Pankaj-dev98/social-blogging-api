package com.geekster.blog.service;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.domainmodel.Post;
import com.geekster.blog.domainmodel.User;
import com.geekster.blog.domainmodel.UserAuthenticationToken;
import com.geekster.blog.persistence.UserAuthenticationTokenDAO;
import com.geekster.blog.persistence.UserDAO;
import com.geekster.blog.uitls.dto.AuthDto;
import com.geekster.blog.uitls.dto.SignInDto;
import com.geekster.blog.uitls.dto.SignUpDto;
import com.geekster.blog.uitls.exceptions.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {
    private UserDAO userDAO;
    private UserAuthenticationTokenDAO tokenDAO;

    @Autowired
    public void setUserDAO(UserDAO userDAO, UserAuthenticationTokenDAO tokenDAO) {
        this.userDAO = userDAO;
        this.tokenDAO = tokenDAO;
    }

    public ResponseEntity<Map<String, String>> signUp(SignUpDto signUpDto) {
        String email = signUpDto.getEmail().toLowerCase();

        if(userDAO.findByEmail(email) != null)
            throw new EmailAlreadyRegisteredException(email + " is already associated with an existing account");

        try {
            User user = userDAO.save(new User(0, signUpDto.getName(), PasswordEncryptor.encrypt(signUpDto.getPassword()),
                    email, signUpDto.getDob(), signUpDto.getGender(), new LinkedList<>(), null, new LinkedList<>()));
            return convertResponse(user);
        }
        catch(NoSuchAlgorithmException e) {
            throw new InternalServerError("Internal server error");
        }
    }

    private static ResponseEntity<Map<String, String>> convertResponse(User user) {
        Map<String, String> map = new HashMap<>();

        map.put("NEW USER ADDED. ID", String.valueOf(user.getId()));
        map.put("Name", user.getName());
        map.put("email", user.getEmail());

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public String signin(SignInDto signInDto) {
        User user = userDAO.findByEmail(signInDto.getEmail());

        if(user == null)
            throw new NoSuchUserException("User with email " + signInDto.getEmail() + " does not exist");

        try {
            if(!user.getPassword().equals(PasswordEncryptor.encrypt(signInDto.getPassword())))
                return "Invalid credentials";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if(user.getAuthToken() != null)
            return "Sign in successful. Token = " + user.getAuthToken().getTokenValue();

        user.setAuthToken(new UserAuthenticationToken(0, UUID.randomUUID().toString(), user));
        userDAO.save(user);
        return "Sign in successful. Token = " + user.getAuthToken().getTokenValue();
    }

    @Transactional
    public String signOut(AuthDto authDto) {
        if (!authenticateUser(authDto))
            throw new InvalidCredentialsException("Invalid combination of email/Credentials. Please try again");

        User user = userDAO.findByEmail(authDto.getEmail());

        tokenDAO.delete(user.getAuthToken());
        user.setAuthToken(null);
        userDAO.save(user);
        return "Signed out successfully";
    }

    public boolean authenticateUser(AuthDto authDto) {
        String email = authDto.getEmail();
        String passedToken = authDto.getToken();

        if(email == null || email.length()  < 1 || passedToken == null || passedToken.length() < 1)
            throw new InvalidCredentialsException("Token or email can't be empty/null");

        User user = userDAO.findByEmail(email);

        if(user == null)
            throw new NoSuchUserException("User with email " + email + " does not exist");

        UserAuthenticationToken token = user.getAuthToken();

        if(token == null)
            return false;

        return passedToken.equals(token.getTokenValue());
    }

    public String followUser(AuthDto authDto, long userToFollowId) {
        if(!authenticateUser(authDto))
            throw new InvalidCredentialsException("Email/Token info does not match");

        User user = userDAO.findByEmail(authDto.getEmail());
        User userToFollow = userDAO.findById(userToFollowId).orElseThrow();

        if(!user.getFollowing().contains(userToFollow)) {
            user.getFollowing().add(userToFollow);
            userToFollow.getFollowers().add(user);
            userDAO.save(user);
            userDAO.save(userToFollow);
            return user.getName() + " now follows " + userToFollow.getName();
        }
        return user.getName() + " already follows " + userToFollow.getName();
    }

    public String unfollowUser(AuthDto authDto, Long userToUnfollowId) {
        if(!authenticateUser(authDto))
            throw new InvalidCredentialsException("Email/Token info does not match");

        User user = userDAO.findByEmail(authDto.getEmail());
        User userToUnfollow = userDAO.findById(userToUnfollowId).orElseThrow();

        user.getFollowing().remove(userToUnfollow);
        userToUnfollow.getFollowers().remove(user);

        userDAO.save(user);
        userDAO.save(userToUnfollow);

        return user.getName() + " unfollowed " + userToUnfollow.getName();
    }

    public ResponseEntity<Map<String, List<String>>> getAllUsers(int page, int size) {
        List<User> users = userDAO.findAll();
        users = getPaginatedSubList(users, page, size);

        if(users.size() == 0)
            throw new EmptyResultListException("No users found");

        Map<String, List<String>> map = new HashMap<>();

        users.forEach(user -> {
           map.put("User id #" + user.getId() + " Name = " + user.getName(),
                   user.getPosts().stream()
                           .map(post -> {
                               return
                               "Post id #" + post.getId() + " " +
                               "Post title " + post.getTitle() + " " +
                               "Post creation Date:" + post.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                           }).toList());
        });

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public Map<String, List<String>> getUserById(long id) {
        var opt = userDAO.findById(id);
        if(opt.isEmpty())
            throw new NoSuchUserException("User with id " + id + " does not exist");

        User user = opt.get();

        Map<String, List<String>> map = new HashMap<>();

        map.put("User id #" + user.getId() + " Name = " + user.getName(),
                user.getPosts().stream()
                        .map(post -> {
                    return
                            "Post id #" + post.getId() + " " +
                            "Post title " + post.getTitle() + " " +
                            "Post creation Date:" + post.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                }).toList());

        return map;
    }

    private static List<User> getPaginatedSubList(List<User> list, int page, int size) {
        int start = page*size;

        if(start >= list.size())
            throw new PageIndexOutOfBoundsException("Max pages index for size " + size + " is "
                    + (list.size()/size) + "[0-indexed]" );

        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    public List<String> getFollowers(AuthDto authDto) {
        if(!authenticateUser(authDto))
            throw new InvalidCredentialsException("Invalid credentials. Please try again");

        User user = userDAO.findByEmail(authDto.getEmail());

        List<String> list = new LinkedList<>();

        user.getFollowers().forEach(follower -> {
            list.add(follower.getId() + " " + follower.getName());
        });

        return list;
    }
}