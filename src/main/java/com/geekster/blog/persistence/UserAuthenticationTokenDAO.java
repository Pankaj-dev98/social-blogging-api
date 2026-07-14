package com.geekster.blog.persistence;

import com.geekster.blog.domainmodel.UserAuthenticationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthenticationTokenDAO extends JpaRepository<UserAuthenticationToken, Long> {
}
