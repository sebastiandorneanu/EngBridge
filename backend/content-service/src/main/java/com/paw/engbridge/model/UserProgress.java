package com.paw.engbridge.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "userprogress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usrprg")
    private Integer idUsrPrg;

    @Column(length = 15)
    private String status;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "users_id_user", nullable = false)
    private Integer userId;

    @Column(name = "courses_id_cs", nullable = false)
    private Integer courseId;

    @Column(name = "courses_levels_id_lvl", nullable = false)
    private Integer levelId;

    @Column(name = "sections_id_sec")
    private Integer sectionId;
}