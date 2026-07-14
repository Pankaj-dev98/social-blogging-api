package com.geekster.blog.persistence;

import com.geekster.blog.domainmodel.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostDao extends JpaRepository<Post, Long> {
    @Query("select e from Post as e where e.title like %:substring%")
    List<Post> findByTitleContaining(@Param("substring") String substring);
}
