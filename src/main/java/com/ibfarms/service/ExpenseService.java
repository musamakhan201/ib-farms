package com.ibfarms.service;

import com.ibfarms.dto.ExpenseDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalExpense;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.exception.BusinessException;
import com.ibfarms.repository.AnimalExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final AnimalExpenseRepository expenseRepository;
    private final AnimalService animalService;

    @Transactional
    public AnimalExpense add(Long animalId, ExpenseDto dto) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        if (animal.getStatus() == AnimalStatus.SOLD) {
            throw new BusinessException("Cannot add expenses to a sold animal");
        }
        AnimalExpense expense = AnimalExpense.builder()
                .animal(animal)
                .expenseDate(dto.getExpenseDate())
                .category(dto.getCategory().trim())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();
        return expenseRepository.save(expense);
    }
}
