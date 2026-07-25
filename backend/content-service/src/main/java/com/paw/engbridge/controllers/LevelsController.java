package com.paw.engbridge.controllers;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Level;
import com.paw.engbridge.services.CourseService;
import com.paw.engbridge.services.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/levels")
public class LevelsController {

    private final LevelService levelService;
    private final CourseService courseService;

    public LevelsController( LevelService levelService,CourseService courseService) {
        this.levelService = levelService;
        this.courseService = courseService;
    }

    @GetMapping("/{levelId}/courses/{orderNum}")
    public ResponseEntity<Course> getSpecificCourse(
            @PathVariable Integer levelId,
            @PathVariable Integer orderNum) {

        return courseService.findByLevelIdAndOrderNum(levelId, orderNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<Level>> getAllLevels() {
        List<Level> levels = levelService.findAll();
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Level> getLevelById(@PathVariable Integer id) {
        return levelService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> getCoursesByLevelId(@PathVariable Integer id) {
        return levelService.findById(id)
                .map(level -> ResponseEntity.ok(level.getCourses()))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Level> createLevel(@RequestBody Level level) {
        Level savedLevel = levelService.save(level);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLevel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Integer id) {
        return levelService.findById(id)
                .map(level -> {
                    levelService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}


