package com.geekster.blog.persistence;

import com.geekster.blog.domainmodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDAO extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
}
