package com.geekster.blog.domainmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment")
public class Comment {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private long commentId;

    @Column(name = "comment_text")
    private String commentText;

    @Column(name = "comment_creation_time")
    private LocalDateTime commentCreationTime;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fk_user_id")
    private User user;
}