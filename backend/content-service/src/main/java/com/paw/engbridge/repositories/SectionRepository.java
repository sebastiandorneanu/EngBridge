package com.paw.engbridge.repositories;

import com.paw.engbridge.model.Course;
import com.paw.engbridge.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {

    @Query("SELECT COUNT(s) FROM Section s WHERE s.course.level.id = :levelId")
    long countByLevelId(@Param("levelId") Integer levelId);

    @Query("SELECT COUNT(s) FROM Section s WHERE s.course.id = :courseId")
    long countByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT c FROM Section c WHERE c.course.id = :courseId AND c.orderNum = :orderNum")
    Optional<Section> findByCourseIdAndOrderNum(
            @Param("courseId") Integer courseId,
            @Param("orderNum") Integer orderNum
    );
}

