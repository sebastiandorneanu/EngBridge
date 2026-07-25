package com.paw.engbridge.repositories;

import com.paw.engbridge.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByLevelId(Integer levelId);

    List<Course> findAllByOrderByOrderNumAsc();
    @Query("SELECT c FROM Course c WHERE c.level.name = :levelName AND c.orderNum = :orderNum")
    Optional<Course> findByLevelNameAndOrderNum(
            @Param("levelName") String levelName,
            @Param("orderNum") Integer orderNum
    );

    @Query("SELECT c FROM Course c WHERE c.level.id = :levelId AND c.orderNum = :orderNum")
    Optional<Course> findByLevelIdAndOrderNum(
            @Param("levelId") Integer levelId,
            @Param("orderNum") Integer orderNum
    );
}

