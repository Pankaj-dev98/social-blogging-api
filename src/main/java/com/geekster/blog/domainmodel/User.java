package com.geekster.blog.domainmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geekster.blog.uitls.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "password", columnDefinition = "VARCHAR(64) NOT NULL")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "dob", columnDefinition = "date")
    private LocalDate dob;

    @Column(name = "gender")
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<Post> posts;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private UserAuthenticationToken authToken;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    List<Comment> userComments;

    @ManyToMany
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private List<User> followers = new LinkedList<>();

    @ManyToMany(mappedBy = "followers")
    private List<User> following = new LinkedList<>();

    // Getters and setters for other fields

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<User> getFollowing() {
        return following;
    }

    public void setFollowing(List<User> following) {
        this.following = following;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", dob=" + dob +
                ", gender=" + gender +
                '}';
    }

    public User(long id, String name, String password,
                String email, LocalDate dob, Gender gender, List<Post> posts,
                UserAuthenticationToken authToken, List<Comment> userComments) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.posts = posts;
        this.authToken = authToken;
        this.userComments = userComments;
    }
}