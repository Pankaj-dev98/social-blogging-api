package com.geekster.blog.domainmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "post_text", columnDefinition = "VARCHAR(512) NOT NULL")
    private String postText;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @ManyToOne
    @JoinColumn(name = "fk_user_id")
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_post_id")
    private List<Comment> comments;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", postText='" + postText + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
}