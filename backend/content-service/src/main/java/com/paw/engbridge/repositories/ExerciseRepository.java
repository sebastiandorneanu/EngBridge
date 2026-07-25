package com.paw.engbridge.repositories;

import com.paw.engbridge.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer>
{
    List<Exercise> findAllBySectionId(Integer sectionId);

    List<Exercise> findAllByType(String type);
}