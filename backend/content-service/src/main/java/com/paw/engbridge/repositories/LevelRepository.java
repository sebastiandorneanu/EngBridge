package com.paw.engbridge.repositories;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<Level, Integer> {

}

