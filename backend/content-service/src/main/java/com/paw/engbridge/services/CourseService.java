package com.paw.engbridge.services;

import aj.org.objectweb.asm.commons.Remapper;
import com.paw.engbridge.model.Course;
import com.paw.engbridge.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Integer id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }
    public void deleteById(Integer id) {
        courseRepository.deleteById(id);
    }

    public List<Course> findAllOrderByOrderNum() {
        return courseRepository.findAllByOrderByOrderNumAsc();
    }
    public Optional<Course> findByLevelNameAndOrderNum(String levelName, Integer orderNum) {
        return courseRepository.findByLevelNameAndOrderNum(levelName, orderNum);
    }

    public Optional<Course> findByLevelIdAndOrderNum(Integer levelId, Integer orderNum) {
        return courseRepository.findByLevelIdAndOrderNum(levelId, orderNum);
    }
}
