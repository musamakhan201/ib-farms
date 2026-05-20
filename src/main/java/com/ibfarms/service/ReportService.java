package com.ibfarms.service;

import com.ibfarms.dto.ReportDto;
import com.ibfarms.entity.AnimalExpense;
import com.ibfarms.entity.AnimalSale;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.GrowthRecord;
import com.ibfarms.entity.OtherExpense;
import com.ibfarms.entity.SalaryPayment;
import com.ibfarms.entity.User;
import com.ibfarms.repository.AnimalExpenseRepository;
import com.ibfarms.repository.AnimalRepository;
import com.ibfarms.repository.AnimalSaleRepository;
import com.ibfarms.repository.GrowthRecordRepository;
import com.ibfarms.repository.OtherExpenseRepository;
import com.ibfarms.repository.SalaryPaymentRepository;
import com.ibfarms.util.FarmProfitCalculator;
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
public class ReportService {

    private final AnimalExpenseRepository expenseRepository;
    private final OtherExpenseRepository otherExpenseRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final AnimalSaleRepository saleRepository;
    private final AnimalRepository animalRepository;
    private final GrowthRecordRepository growthRecordRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public ReportDto build(String type, LocalDate from, LocalDate to) {
        User owner = currentUserService.getCurrentUser();
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate toDate = to != null ? to : LocalDate.now();

        return switch (type != null ? type : "expenses") {
            case "sales" -> salesReport(owner.getId(), fromDate, toDate);
            case "profit" -> profitReport(owner.getId(), fromDate, toDate);
            case "pregnancy" -> pregnancyReport(owner);
            case "growth" -> growthReport(owner.getId(), fromDate, toDate);
            default -> expensesReport(owner.getId(), fromDate, toDate);
        };
    }

    private ReportDto expensesReport(Long ownerId, LocalDate from, LocalDate to) {
        List<AnimalExpense> animalList = expenseRepository.findByOwnerAndDateRange(ownerId, from, to);
        List<OtherExpense> otherList = otherExpenseRepository.findByOwnerAndDateRange(ownerId, from, to);
        List<SalaryPayment> salaryList = salaryPaymentRepository.findByOwnerAndPaidDateRange(ownerId, from, to);

        BigDecimal animalTotal = expenseRepository.sumByOwnerAndDateRange(ownerId, from, to);
        BigDecimal otherTotal = otherExpenseRepository.sumByOwnerAndDateRange(ownerId, from, to);
        BigDecimal salaryTotal = salaryPaymentRepository.sumByOwnerAndPaidDateRange(ownerId, from, to);
        BigDecimal combined = animalTotal.add(otherTotal).add(salaryTotal);

        List<ReportDto.ExpenseLineDto> lines = new ArrayList<>();
        for (AnimalExpense e : animalList) {
            lines.add(ReportDto.ExpenseLineDto.builder()
                    .expenseType("Animal")
                    .date(e.getExpenseDate())
                    .animalName(e.getAnimal().getName())
                    .tagNumber(e.getAnimal().getTagNumber())
                    .category(e.getCategory())
                    .amount(e.getAmount())
                    .description(e.getDescription())
                    .build());
        }
        for (OtherExpense o : otherList) {
            lines.add(ReportDto.ExpenseLineDto.builder()
                    .expenseType("Other")
                    .date(o.getExpenseDate())
                    .category(o.getCategory())
                    .amount(o.getAmount())
                    .description(o.getDescription())
                    .build());
        }
        for (SalaryPayment s : salaryList) {
            lines.add(ReportDto.ExpenseLineDto.builder()
                    .expenseType("Salary")
                    .date(s.getPaidDate())
                    .animalName(s.getEmployee().getFullName())
                    .tagNumber(s.getMonthLabel())
                    .category("Salary")
                    .amount(s.getAmount())
                    .description(s.getNotes())
                    .build());
        }

        List<Object[]> byCategory = expenseRepository.sumByCategory(ownerId, from, to);
        List<ReportDto.CategoryTotalDto> categories = new ArrayList<>(byCategory.stream()
                .map(row -> ReportDto.CategoryTotalDto.builder()
                        .category("Animal: " + row[0])
                        .total((BigDecimal) row[1])
                        .build())
                .toList());
        if (otherTotal.signum() > 0) {
            categories.add(ReportDto.CategoryTotalDto.builder().category("Other expenses").total(otherTotal).build());
        }
        if (salaryTotal.signum() > 0) {
            categories.add(ReportDto.CategoryTotalDto.builder().category("Salaries").total(salaryTotal).build());
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        for (ReportDto.CategoryTotalDto c : categories) {
            labels.add(c.getCategory());
            data.add(c.getTotal());
        }

        return ReportDto.builder()
                .reportType("expenses")
                .from(from)
                .to(to)
                .totalExpenses(combined)
                .totalAnimalExpenses(animalTotal)
                .totalOtherExpenses(otherTotal)
                .totalSalaryPayments(salaryTotal)
                .expenses(lines)
                .expenseByCategory(categories)
                .chartLabels(labels)
                .chartData(data)
                .build();
    }

    private ReportDto salesReport(Long ownerId, LocalDate from, LocalDate to) {
        List<AnimalSale> sales = saleRepository.findByOwnerAndDateRange(ownerId, from, to);
        BigDecimal total = saleRepository.sumSalePriceByOwnerAndDateRange(ownerId, from, to);

        List<ReportDto.SaleLineDto> lines = sales.stream()
                .map(s -> {
                    BigDecimal expenses = expenseRepository.sumByAnimal(s.getAnimal());
                    BigDecimal profit = ProfitCalculator.calculate(
                            s.getSalePrice(), s.getAnimal().getPurchasePrice(), expenses);
                    return ReportDto.SaleLineDto.builder()
                            .saleDate(s.getSaleDate())
                            .animalName(s.getAnimal().getName())
                            .tagNumber(s.getAnimal().getTagNumber())
                            .salePrice(s.getSalePrice())
                            .purchasePrice(s.getAnimal().getPurchasePrice())
                            .expenses(expenses)
                            .profit(profit)
                            .buyerName(s.getBuyerName())
                            .build();
                })
                .toList();

        return ReportDto.builder()
                .reportType("sales")
                .from(from)
                .to(to)
                .totalSales(total)
                .sales(lines)
                .build();
    }

    private ReportDto profitReport(Long ownerId, LocalDate from, LocalDate to) {
        List<AnimalSale> sales = saleRepository.findByOwnerAndDateRange(ownerId, from, to);
        List<ReportDto.ProfitLineDto> lines = new ArrayList<>();
        BigDecimal grossAnimalProfit = BigDecimal.ZERO;

        for (AnimalSale s : sales) {
            BigDecimal animalExpenses = expenseRepository.sumByAnimal(s.getAnimal());
            BigDecimal profit = ProfitCalculator.calculate(
                    s.getSalePrice(), s.getAnimal().getPurchasePrice(), animalExpenses);
            grossAnimalProfit = grossAnimalProfit.add(profit);
            lines.add(ReportDto.ProfitLineDto.builder()
                    .animalName(s.getAnimal().getName())
                    .tagNumber(s.getAnimal().getTagNumber())
                    .salePrice(s.getSalePrice())
                    .purchasePrice(s.getAnimal().getPurchasePrice())
                    .expenses(animalExpenses)
                    .profit(profit)
                    .build());
        }

        BigDecimal otherTotal = otherExpenseRepository.sumByOwnerAndDateRange(ownerId, from, to);
        BigDecimal salaryTotal = salaryPaymentRepository.sumByOwnerAndPaidDateRange(ownerId, from, to);
        BigDecimal netFarmProfit = FarmProfitCalculator.netFarmProfit(grossAnimalProfit, otherTotal, salaryTotal);

        List<ReportDto.OtherExpenseLineDto> otherLines = otherExpenseRepository.findByOwnerAndDateRange(ownerId, from, to)
                .stream()
                .map(o -> ReportDto.OtherExpenseLineDto.builder()
                        .date(o.getExpenseDate())
                        .category(o.getCategory())
                        .amount(o.getAmount())
                        .description(o.getDescription())
                        .build())
                .toList();

        List<ReportDto.SalaryLineDto> salaryLines = salaryPaymentRepository.findByOwnerAndPaidDateRange(ownerId, from, to)
                .stream()
                .map(s -> ReportDto.SalaryLineDto.builder()
                        .paidDate(s.getPaidDate())
                        .employeeName(s.getEmployee().getFullName())
                        .role(s.getEmployee().getRole())
                        .monthLabel(s.getMonthLabel())
                        .amount(s.getAmount())
                        .notes(s.getNotes())
                        .build())
                .toList();

        return ReportDto.builder()
                .reportType("profit")
                .from(from)
                .to(to)
                .grossAnimalProfit(grossAnimalProfit)
                .totalOtherExpenses(otherTotal)
                .totalSalaryPayments(salaryTotal)
                .netFarmProfit(netFarmProfit)
                .totalProfit(netFarmProfit)
                .profits(lines)
                .otherExpenses(otherLines)
                .salaryPayments(salaryLines)
                .build();
    }

    private ReportDto pregnancyReport(User owner) {
        List<ReportDto.PregnancyLineDto> lines = animalRepository
                .findByOwnerAndPregnantTrueAndStatusOrderByExpectedDeliveryDateAsc(owner, AnimalStatus.ACTIVE)
                .stream()
                .map(a -> ReportDto.PregnancyLineDto.builder()
                        .name(a.getName())
                        .tagNumber(a.getTagNumber())
                        .species(a.getSpecies())
                        .pregnancyDate(a.getPregnancyDate())
                        .expectedDeliveryDate(a.getExpectedDeliveryDate())
                        .daysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), a.getExpectedDeliveryDate()))
                        .build())
                .toList();

        return ReportDto.builder()
                .reportType("pregnancy")
                .from(LocalDate.now())
                .to(LocalDate.now().plusMonths(3))
                .pregnancies(lines)
                .build();
    }

    private ReportDto growthReport(Long ownerId, LocalDate from, LocalDate to) {
        List<GrowthRecord> records = growthRecordRepository.findByAnimalOwnerIdOrderByRecordDateDesc(ownerId)
                .stream()
                .filter(r -> !r.getRecordDate().isBefore(from) && !r.getRecordDate().isAfter(to))
                .toList();

        List<ReportDto.GrowthLineDto> lines = records.stream()
                .map(r -> ReportDto.GrowthLineDto.builder()
                        .animalName(r.getAnimal().getName())
                        .tagNumber(r.getAnimal().getTagNumber())
                        .recordDate(r.getRecordDate())
                        .heightCm(r.getHeightCm())
                        .lengthCm(r.getLengthCm())
                        .weightKg(r.getWeightKg())
                        .build())
                .toList();

        return ReportDto.builder()
                .reportType("growth")
                .from(from)
                .to(to)
                .growth(lines)
                .build();
    }
}
