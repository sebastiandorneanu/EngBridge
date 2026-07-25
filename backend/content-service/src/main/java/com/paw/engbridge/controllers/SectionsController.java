package com.paw.engbridge.controllers;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Exercise;
import com.paw.engbridge.model.Level;
import com.paw.engbridge.model.Section;
import com.paw.engbridge.services.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sections")
public class SectionsController {

    private final SectionService sectionService;

    public SectionsController( SectionService sectionService) {
        this.sectionService = sectionService;
    }
    @GetMapping
    public ResponseEntity<List<Section>> getAllLevels() {
        List<Section> sections = sectionService.findAll();
        return ResponseEntity.ok(sections);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Section> getLevelById(@PathVariable Integer id) {
        return sectionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/exercises")
    public ResponseEntity<List<Exercise>> getExerciseBySectionId(@PathVariable Integer id) {
        return sectionService.findById(id)
                .map(section -> ResponseEntity.ok(section.getExercises()))
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<Section> createLevel(@RequestBody Section section) {
        Section savedSection = sectionService.save(section);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSection);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Integer id) {
        return sectionService.findById(id)
                .map(level -> {
                    sectionService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


}
