package com.paw.engbridge.services;

import com.paw.engbridge.model.Exercise;
import com.paw.engbridge.repositories.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExerciseService {
    @Autowired
    private ExerciseRepository exerciseRepository;

    public List<Exercise> findAll()
    {
        return exerciseRepository.findAll();
    }

    public Optional<Exercise> findById(Integer id)
    {
        return exerciseRepository.findById(id);
    }

    public Exercise save(Exercise exercise)
    {
        if (exercise.getContent() == null || exercise.getContent().isEmpty()) {
            throw new IllegalArgumentException("Exercise content cannot be empty.");
        }
        return exerciseRepository.save(exercise);
    }

    public void deleteById(Integer id)
    {
        exerciseRepository.deleteById(id);
    }

    public List<Exercise> findBySection(Integer sectionID)
    {
        return exerciseRepository.findAllBySectionId(sectionID);
    }

    public List<Exercise> findExercisesByType(String type) {
        return exerciseRepository.findAllByType(type);
    }
}
