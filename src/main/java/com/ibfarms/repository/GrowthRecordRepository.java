package com.ibfarms.repository;

import com.ibfarms.entity.Animal;
import com.ibfarms.entity.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {

    List<GrowthRecord> findByAnimalOrderByRecordDateAsc(Animal animal);

    List<GrowthRecord> findByAnimalOwnerIdOrderByRecordDateDesc(Long ownerId);
}
