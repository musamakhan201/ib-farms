package com.ibfarms.service;

import com.ibfarms.dto.SaleDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalSale;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.exception.BusinessException;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.repository.AnimalSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final AnimalSaleRepository saleRepository;
    private final AnimalExpenseRepository expenseRepository;
    private final AnimalService animalService;

    @Transactional
    public AnimalSale sell(Long animalId, SaleDto dto) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        if (animal.getStatus() == AnimalStatus.SOLD) {
            throw new BusinessException("Animal is already sold");
        }
        if (saleRepository.findByAnimalId(animalId).isPresent()) {
            throw new BusinessException("Sale record already exists");
        }

        AnimalSale sale = AnimalSale.builder()
                .animal(animal)
                .saleDate(dto.getSaleDate())
                .salePrice(dto.getSalePrice())
                .buyerName(dto.getBuyerName() != null ? dto.getBuyerName().trim() : null)
                .notes(dto.getNotes())
                .build();

        animal.setStatus(AnimalStatus.SOLD);
        animal.setPregnant(false);
        animal.setPregnancyDate(null);
        animal.setExpectedDeliveryDate(null);
        animal.setSale(sale);

        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public BigDecimal previewProfit(Long animalId, BigDecimal salePrice) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        BigDecimal expenses = expenseRepository.sumByAnimal(animal);
        return com.ibfarms.util.ProfitCalculator.calculate(salePrice, animal.getPurchasePrice(), expenses);
    }
}
