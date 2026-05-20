package com.ibfarms.service;

import com.ibfarms.dto.SaleReportDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalExpense;
import com.ibfarms.entity.AnimalSale;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.util.ProfitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleReportService {

    private final AnimalService animalService;
    private final AnimalExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public SaleReportDto build(Long animalId) {
        Animal animal = animalService.getOwnedAnimal(animalId);
        AnimalSale sale = animal.getSale();
        if (sale == null) {
            throw new ResourceNotFoundException("No sale record for this animal");
        }

        List<AnimalExpense> expenses = expenseRepository.findByAnimalOrderByExpenseDateDesc(animal);
        BigDecimal totalExpenses = expenseRepository.sumByAnimal(animal);
        BigDecimal profit = ProfitCalculator.calculate(sale.getSalePrice(), animal.getPurchasePrice(), totalExpenses);

        List<SaleReportDto.ExpenseLine> lines = expenses.stream()
                .map(e -> SaleReportDto.ExpenseLine.builder()
                        .date(e.getExpenseDate())
                        .category(e.getCategory())
                        .amount(e.getAmount())
                        .description(e.getDescription())
                        .build())
                .toList();

        return SaleReportDto.builder()
                .animalId(animal.getId())
                .animalName(animal.getName())
                .tagNumber(animal.getTagNumber())
                .species(animal.getSpecies())
                .breed(animal.getBreed())
                .purchaseDate(animal.getPurchaseDate())
                .purchasePrice(animal.getPurchasePrice())
                .saleDate(sale.getSaleDate())
                .salePrice(sale.getSalePrice())
                .buyerName(sale.getBuyerName())
                .saleNotes(sale.getNotes())
                .totalExpenses(totalExpenses)
                .profit(profit)
                .profitLoss(profit.signum() < 0)
                .expenses(lines)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))
                .build();
    }
}
