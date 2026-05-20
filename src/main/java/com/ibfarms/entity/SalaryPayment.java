package com.ibfarms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "salary_payments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "salary_year", "salary_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "salary_year", nullable = false)
    private int salaryYear;

    @Column(name = "salary_month", nullable = false)
    private int salaryMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paidDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getMonthLabel() {
        return java.time.YearMonth.of(salaryYear, salaryMonth)
                .format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
    }
}
