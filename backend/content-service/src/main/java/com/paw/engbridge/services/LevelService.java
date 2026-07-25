package com.paw.engbridge.services;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Level;
import com.paw.engbridge.repositories.CourseRepository;
import com.paw.engbridge.repositories.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LevelService {
    @Autowired
    private LevelRepository levelRepository;

    public List<Level> findAll() {
        return levelRepository.findAll();
    }

    public Optional<Level> findById(Integer id) {
        return levelRepository.findById(id);
    }

    public Level save(Level level) {
        return levelRepository.save(level);
    }
    public void deleteById(Integer id) {
        levelRepository.deleteById(id);
    }

}

