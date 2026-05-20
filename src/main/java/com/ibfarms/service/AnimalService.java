package com.ibfarms.service;

import com.ibfarms.dto.AnimalDetailDto;
import com.ibfarms.dto.AnimalFormDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.User;
import com.ibfarms.exception.BusinessException;
import com.ibfarms.exception.DuplicateResourceException;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.repository.AnimalRepository;
import com.ibfarms.util.CloudinaryImageService;
import com.ibfarms.util.PregnancyCalculator;
import com.ibfarms.util.ProfitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalExpenseRepository expenseRepository;
    private final CurrentUserService currentUserService;
    private final CloudinaryImageService cloudinaryImageService;
    @Transactional(readOnly = true)
    public Page<Animal> search(String search, String species, AnimalStatus status, Boolean pregnant, Pageable pageable) {
        User owner = currentUserService.getCurrentUser();
        return animalRepository.search(owner, emptyToNull(search), emptyToNull(species), status, pregnant, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> speciesOptions() {
        return animalRepository.findDistinctSpeciesByOwner(currentUserService.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public Animal getOwnedAnimal(Long id) {
        User owner = currentUserService.getCurrentUser();
        return animalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));
    }

    @Transactional(readOnly = true)
    public AnimalDetailDto getDetail(Long id) {
        Animal animal = getOwnedAnimal(id);
        BigDecimal totalExpenses = expenseRepository.sumByAnimal(animal);
        BigDecimal profit = null;
        AnimalDetailDto.SaleSummaryDto saleDto = null;

        if (animal.getSale() != null) {
            profit = ProfitCalculator.calculate(
                    animal.getSale().getSalePrice(),
                    animal.getPurchasePrice(),
                    totalExpenses);
            saleDto = AnimalDetailDto.SaleSummaryDto.builder()
                    .saleDate(animal.getSale().getSaleDate())
                    .salePrice(animal.getSale().getSalePrice())
                    .buyerName(animal.getSale().getBuyerName())
                    .notes(animal.getSale().getNotes())
                    .profit(profit)
                    .build();
        }

        return AnimalDetailDto.builder()
                .id(animal.getId())
                .name(animal.getName())
                .species(animal.getSpecies())
                .breed(animal.getBreed())
                .tagNumber(animal.getTagNumber())
                .purchasePrice(animal.getPurchasePrice())
                .purchaseDate(animal.getPurchaseDate())
                .pictureFilename(cloudinaryImageService.buildUrl(animal.getPictureFilename()))
                .pregnant(animal.isPregnant())
                .pregnancyDate(animal.getPregnancyDate())
                .expectedDeliveryDate(animal.getExpectedDeliveryDate())
                .status(animal.getStatus())
                .totalExpenses(totalExpenses)
                .profit(profit)
                .sale(saleDto)
                .growthRecords(animal.getGrowthRecords().stream()
                        .map(g -> AnimalDetailDto.GrowthSummaryDto.builder()
                                .id(g.getId())
                                .recordDate(g.getRecordDate())
                                .heightCm(g.getHeightCm())
                                .lengthCm(g.getLengthCm())
                                .weightKg(g.getWeightKg())
                                .notes(g.getNotes())
                                .build())
                        .toList())
                .expenses(animal.getExpenses().stream()
                        .map(e -> AnimalDetailDto.ExpenseSummaryDto.builder()
                                .id(e.getId())
                                .expenseDate(e.getExpenseDate())
                                .category(e.getCategory())
                                .amount(e.getAmount())
                                .description(e.getDescription())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public Animal create(AnimalFormDto dto, MultipartFile picture) throws IOException {
        User owner = currentUserService.getCurrentUser();
        if (animalRepository.existsByTagNumberAndOwner(dto.getTagNumber().trim(), owner)) {
            throw new DuplicateResourceException("Tag number already exists");
        }
        Animal animal = mapToEntity(new Animal(), dto, owner);
        if (picture != null && !picture.isEmpty()) {
            animal.setPictureFilename(cloudinaryImageService.upload(picture));
        }
        return animalRepository.save(animal);
    }

    @Transactional
    public Animal update(Long id, AnimalFormDto dto, MultipartFile picture) throws IOException {
        Animal animal = getOwnedAnimal(id);
        if (animal.getStatus() == AnimalStatus.SOLD) {
            throw new BusinessException("Cannot edit a sold animal");
        }
        User owner = animal.getOwner();
        if (animalRepository.existsByTagNumberAndOwnerAndIdNot(dto.getTagNumber().trim(), owner, id)) {
            throw new DuplicateResourceException("Tag number already exists");
        }
        mapToEntity(animal, dto, owner);
        if (picture != null && !picture.isEmpty()) {
            cloudinaryImageService.deleteIfExists(animal.getPictureFilename());
            animal.setPictureFilename(cloudinaryImageService.upload(picture));
        }
        return animalRepository.save(animal);
    }

    @Transactional
    public void delete(Long id) throws IOException {
        Animal animal = getOwnedAnimal(id);
        if (animal.getStatus() == AnimalStatus.SOLD) {
            throw new BusinessException("Cannot delete a sold animal");
        }
        cloudinaryImageService.deleteIfExists(animal.getPictureFilename());
        animalRepository.delete(animal);
    }

    public AnimalFormDto toFormDto(Animal animal) {
        AnimalFormDto dto = new AnimalFormDto();
        dto.setId(animal.getId());
        dto.setName(animal.getName());
        dto.setSpecies(animal.getSpecies());
        dto.setBreed(animal.getBreed());
        dto.setTagNumber(animal.getTagNumber());
        dto.setPurchasePrice(animal.getPurchasePrice());
        dto.setPurchaseDate(animal.getPurchaseDate());
        dto.setPregnant(animal.isPregnant());
        dto.setPregnancyDate(animal.getPregnancyDate());
        dto.setExistingPictureFilename(animal.getPictureFilename());
        return dto;
    }

    private Animal mapToEntity(Animal animal, AnimalFormDto dto, User owner) {
        animal.setName(dto.getName().trim());
        animal.setSpecies(dto.getSpecies().trim());
        animal.setBreed(dto.getBreed() != null ? dto.getBreed().trim() : null);
        animal.setTagNumber(dto.getTagNumber().trim());
        animal.setPurchasePrice(dto.getPurchasePrice());
        animal.setPurchaseDate(dto.getPurchaseDate());
        animal.setOwner(owner);
        applyPregnancy(animal, dto.isPregnant(), dto.getPregnancyDate());
        return animal;
    }

    private void applyPregnancy(Animal animal, boolean pregnant, java.time.LocalDate pregnancyDate) {
        animal.setPregnant(pregnant);
        if (pregnant) {
            if (pregnancyDate == null) {
                throw new BusinessException("Pregnancy date is required when animal is marked pregnant");
            }
            animal.setPregnancyDate(pregnancyDate);
            animal.setExpectedDeliveryDate(PregnancyCalculator.expectedDeliveryDate(pregnancyDate));
        } else {
            animal.setPregnancyDate(null);
            animal.setExpectedDeliveryDate(null);
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
