package com.ibfarms.service;

import com.ibfarms.dto.GrowthRecordDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.GrowthRecord;
import com.ibfarms.exception.BusinessException;
import com.ibfarms.repository.GrowthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthRecordService {

    private final GrowthRecordRepository growthRecordRepository;
    private final AnimalService animalService;

    @Transactional
    public GrowthRecord add(Long animalId, GrowthRecordDto dto) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        if (animal.getStatus() == AnimalStatus.SOLD) {
            throw new BusinessException("Cannot add growth records to a sold animal");
        }
        if (dto.getHeightCm() == null && dto.getLengthCm() == null && dto.getWeightKg() == null) {
            throw new BusinessException("At least one measurement (height, length, or weight) is required");
        }
        GrowthRecord record = GrowthRecord.builder()
                .animal(animal)
                .recordDate(dto.getRecordDate())
                .heightCm(dto.getHeightCm())
                .lengthCm(dto.getLengthCm())
                .weightKg(dto.getWeightKg())
                .notes(dto.getNotes())
                .build();
        return growthRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<GrowthRecord> findByAnimal(Long animalId) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        return growthRecordRepository.findByAnimalOrderByRecordDateAsc(animal);
    }
}
