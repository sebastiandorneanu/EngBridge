package com.paw.engbridge.controllers;

import com.paw.engbridge.model.Exercise;
import com.paw.engbridge.services.ExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
public class ExerciseController
{
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService)
    {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public ResponseEntity<List<Exercise>> getAllExercises()
    {
        List<Exercise> exercises = exerciseService.findAll();
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable Integer id) {
        return exerciseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Exercise> createExercise(@RequestBody Exercise exercise)
    {
        Exercise savedExercise = exerciseService.save(exercise);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExercise);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Integer id)
    {
        if (exerciseService.findById(id).isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        exerciseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Exercise>> getExercisesBySection(@PathVariable Integer sectionId)
    {
        List<Exercise> exercises = exerciseService.findBySection(sectionId);

        if (exercises.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Exercise>> getExercisesByType(@PathVariable String type)
    {
        List<Exercise> exercises = exerciseService.findExercisesByType(type);

        if (exercises.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(exercises);
    }


}
