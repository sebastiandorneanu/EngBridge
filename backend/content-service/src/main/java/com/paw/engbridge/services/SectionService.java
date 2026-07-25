package com.paw.engbridge.services;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Level;
import com.paw.engbridge.model.Section;
import com.paw.engbridge.repositories.LevelRepository;
import com.paw.engbridge.repositories.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectionService {
    @Autowired
    private SectionRepository sectionRepository;

    public List<Section> findAll() {
        return sectionRepository.findAll();
    }

    public Optional<Section> findById(Integer id) {
        return sectionRepository.findById(id);
    }

    public Section save(Section section) {
        return sectionRepository.save(section);
    }
    public void deleteById(Integer id) {
        sectionRepository.deleteById(id);
    }

    public Optional<Section> findByLevelIdAndOrderNum(Integer courseID, Integer orderNum) {
        return sectionRepository.findByCourseIdAndOrderNum(courseID, orderNum);
    }
}
