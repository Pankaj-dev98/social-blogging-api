package com.geekster.blog.persistence;

import com.geekster.blog.domainmodel.Comment;
import com.geekster.blog.domainmodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentDAO extends JpaRepository<Comment, Long> {
    List<Comment> findByUser(User user);
    Comment findByCommentIdAndUser(long commentId, User user);
}
