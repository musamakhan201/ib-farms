package com.ibfarms.service;

import com.ibfarms.dto.DashboardDto;
import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.User;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.repository.AnimalRepository;
import com.ibfarms.repository.AnimalSaleRepository;
import com.ibfarms.repository.OtherExpenseRepository;
import com.ibfarms.repository.SalaryPaymentRepository;
import com.ibfarms.util.ProfitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnimalRepository animalRepository;
    private final AnimalExpenseRepository animalExpenseRepository;
    private final OtherExpenseRepository otherExpenseRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final AnimalSaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public DashboardDto build() {
        User owner = currentUserService.getCurrentUser();
        Long ownerId = owner.getId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long total = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.ACTIVE)
                + animalRepository.countByOwnerAndStatus(owner, AnimalStatus.SOLD);
        long active = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.ACTIVE);
        long sold = animalRepository.countByOwnerAndStatus(owner, AnimalStatus.SOLD);
        long pregnant = animalRepository.countByOwnerAndPregnantTrueAndStatus(owner, AnimalStatus.ACTIVE);

        BigDecimal animalExpenses = animalExpenseRepository.sumByOwnerAndDateRange(ownerId, monthStart, monthEnd);
        BigDecimal otherExpenses = otherExpenseRepository.sumByOwnerAndDateRange(ownerId, monthStart, monthEnd);
        BigDecimal salaryExpenses = salaryPaymentRepository.sumByOwnerAndPaidDateRange(ownerId, monthStart, monthEnd);
        BigDecimal monthlyExpenses = animalExpenses.add(otherExpenses).add(salaryExpenses);

        BigDecimal monthlySales = saleRepository.sumSalePriceByOwnerAndDateRange(ownerId, monthStart, monthEnd);

        List<AnimalSaleProfit> saleProfits = computeMonthlySaleProfits(ownerId, monthStart, monthEnd);
        BigDecimal monthlyProfit = saleProfits.stream()
                .map(AnimalSaleProfit::profit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LocalDate, BigDecimal> animalByDay = toDailyMap(animalExpenseRepository.sumByDay(ownerId, monthStart, monthEnd));
        Map<LocalDate, BigDecimal> otherByDay = toDailyMap(otherExpenseRepository.sumByDay(ownerId, monthStart, monthEnd));
        Map<LocalDate, BigDecimal> salaryByDay = toDailyMap(salaryPaymentRepository.sumByDay(ownerId, monthStart, monthEnd));

        TreeSet<LocalDate> allDates = new TreeSet<>();
        allDates.addAll(animalByDay.keySet());
        allDates.addAll(otherByDay.keySet());
        allDates.addAll(salaryByDay.keySet());

        List<String> expenseLabels = new ArrayList<>();
        List<Double> expenseAnimalData = new ArrayList<>();
        List<Double> expenseOtherData = new ArrayList<>();
        List<Double> expenseSalaryData = new ArrayList<>();
        for (LocalDate date : allDates) {
            expenseLabels.add(date.toString());
            expenseAnimalData.add(amountForDay(animalByDay, date));
            expenseOtherData.add(amountForDay(otherByDay, date));
            expenseSalaryData.add(amountForDay(salaryByDay, date));
        }

        List<Object[]> salesByDay = saleRepository.findByOwnerAndDateRange(ownerId, monthStart, monthEnd)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getSaleDate(),
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO,
                                s -> s.getSalePrice(),
                                BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();

        List<String> salesLabels = new ArrayList<>();
        List<Double> salesData = new ArrayList<>();
        for (Object[] row : salesByDay) {
            salesLabels.add(row[0].toString());
            salesData.add(((BigDecimal) row[1]).doubleValue());
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
                .expenseChartAnimalData(expenseAnimalData)
                .expenseChartOtherData(expenseOtherData)
                .expenseChartSalaryData(expenseSalaryData)
                .salesChartLabels(salesLabels)
                .salesChartData(salesData)
                .upcomingDeliveries(deliveries)
                .build();
    }

    private Map<LocalDate, BigDecimal> toDailyMap(List<Object[]> rows) {
        Map<LocalDate, BigDecimal> map = new TreeMap<>();
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            map.put(date, amount);
        }
        return map;
    }

    private double amountForDay(Map<LocalDate, BigDecimal> byDay, LocalDate date) {
        return byDay.getOrDefault(date, BigDecimal.ZERO).doubleValue();
    }

    private List<AnimalSaleProfit> computeMonthlySaleProfits(Long ownerId, LocalDate from, LocalDate to) {
        return saleRepository.findByOwnerAndDateRange(ownerId, from, to).stream()
                .map(sale -> {
                    BigDecimal expenses = animalExpenseRepository.sumByAnimal(sale.getAnimal());
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
