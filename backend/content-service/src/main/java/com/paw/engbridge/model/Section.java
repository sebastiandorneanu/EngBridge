package com.paw.engbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "sections")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column()
    private Integer orderNum;

    @Column(length = 15)
    private String type;

    @ManyToOne
    @JoinColumn(name="sections_course_fk")
    @JsonIgnore
    private Course course;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Exercise> exercises;


}
