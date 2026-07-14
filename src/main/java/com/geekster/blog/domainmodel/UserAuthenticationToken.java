package com.geekster.blog.domainmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_authentication_token")
public class UserAuthenticationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long token_id;

    @Column(name = "token_value")
    private String tokenValue;

    @OneToOne
    @JoinColumn(name = "fk_user_id")
    private User user;
}

