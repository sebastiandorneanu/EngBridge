package com.paw.engbridge.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_user;

    @Column(nullable = false)
    private String username;
    private String password;
    private String email;
    private Integer levels_id_lvl;

    private String role;

    @Column(name = "placement_test_score")
    private Integer placementTestScore;

}
