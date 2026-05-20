package com.ibfarms.service;

import com.ibfarms.dto.DashboardDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.User;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.repository.AnimalRepository;
import com.ibfarms.repository.AnimalSaleRepository;
import com.ibfarms.util.ProfitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnimalRepository animalRepository;
    private final AnimalExpenseRepository expenseRepository;
    private final AnimalSaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public DashboardDto build() {
        User owner = currentUserService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long total = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.ACTIVE)
                + animalRepository.countByOwnerAndStatus(owner, AnimalStatus.SOLD);
        long active = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.ACTIVE);
        long sold = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.SOLD);
        long pregnant = animalRepository.countByOwnerAndPregnantTrueAndStatus(owner, AnimalStatus.ACTIVE);

        BigDecimal monthlyExpenses = expenseRepository.sumByOwnerAndDateRange(owner.getId(), monthStart, monthEnd);
        BigDecimal monthlySales = saleRepository.sumSalePriceByOwnerAndDateRange(owner.getId(), monthStart, monthEnd);

        List<AnimalSaleProfit> saleProfits = computeMonthlySaleProfits(owner.getId(), monthStart, monthEnd);
        BigDecimal monthlyProfit = saleProfits.stream()
                .map(AnimalSaleProfit::profit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Object[]> expenseByDay = expenseRepository.sumByDay(owner.getId(), monthStart, monthEnd);
        List<String> expenseLabels = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();
        for (Object[] row : expenseByDay) {
            expenseLabels.add(row[0].toString());
            expenseData.add((BigDecimal) row[1]);
        }

        List<Object[]> salesByDay = saleRepository.findByOwnerAndDateRange(owner.getId(), monthStart, monthEnd)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getSaleDate(),
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO,
                                s -> s.getSalePrice(),
                                BigDecimal::add)))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();

        List<String> salesLabels = new ArrayList<>();
        List<BigDecimal> salesData = new ArrayList<>();
        for (Object[] row : salesByDay) {
            salesLabels.add(row[0].toString());
            salesData.add((BigDecimal) row[1]);
        }

        List<DashboardDto.PregnancyAlertDto> deliveries = animalRepository
                .findByOwnerAndPregnantTrueAndStatusOrderByExpectedDeliveryDateAsc(owner, AnimalStatus.ACTIVE)
                .stream()
                .map(this::toPregnancyAlert)
                .toList();

        return DashboardDto.builder()
                .totalAnimals(total)
                .activeAnimals(active)
                .soldAnimals(sold)
                .pregnantAnimals(pregnant)
                .monthlyExpenses(monthlyExpenses)
                .monthlySales(monthlySales)
                .monthlyProfit(monthlyProfit)
                .expenseChartLabels(expenseLabels)
                .expenseChartData(expenseData)
                .salesChartLabels(salesLabels)
                .salesChartData(salesData)
                .upcomingDeliveries(deliveries)
                .build();
    }

    private List<AnimalSaleProfit> computeMonthlySaleProfits(Long ownerId, LocalDate from, LocalDate to) {
        return saleRepository.findByOwnerAndDateRange(ownerId, from, to).stream()
                .map(sale -> {
                    BigDecimal expenses = expenseRepository.sumByAnimal(sale.getAnimal());
                    BigDecimal profit = ProfitCalculator.calculate(
                            sale.getSalePrice(),
                            sale.getAnimal().getPurchasePrice(),
                            expenses);
                    return new AnimalSaleProfit(profit);
                })
                .toList();
    }

    private DashboardDto.PregnancyAlertDto toPregnancyAlert(Animal animal) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), animal.getExpectedDeliveryDate());
        return DashboardDto.PregnancyAlertDto.builder()
                .animalId(animal.getId())
                .name(animal.getName())
                .tagNumber(animal.getTagNumber())
                .expectedDeliveryDate(animal.getExpectedDeliveryDate().toString())
                .daysRemaining(days)
                .build();
    }

    private record AnimalSaleProfit(BigDecimal profit) {
    }
}
