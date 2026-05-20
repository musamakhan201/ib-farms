package com.ibfarms.repository;

import com.ibfarms.entity.Employee;
import com.ibfarms.entity.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {

    List<SalaryPayment> findByEmployeeOrderBySalaryYearDescSalaryMonthDesc(Employee employee);

    Optional<SalaryPayment> findByEmployeeAndSalaryYearAndSalaryMonth(
            Employee employee, int salaryYear, int salaryMonth);

    @Query("""
            SELECT COALESCE(SUM(s.amount), 0) FROM SalaryPayment s
            WHERE s.employee.owner.id = :ownerId
              AND s.paidDate BETWEEN :from AND :to
            """)
    BigDecimal sumByOwnerAndPaidDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT s FROM SalaryPayment s
            WHERE s.employee.owner.id = :ownerId
              AND s.paidDate BETWEEN :from AND :to
            ORDER BY s.paidDate DESC
            """)
    List<SalaryPayment> findByOwnerAndPaidDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT s.paidDate, SUM(s.amount) FROM SalaryPayment s
            WHERE s.employee.owner.id = :ownerId
              AND s.paidDate BETWEEN :from AND :to
            GROUP BY s.paidDate
            ORDER BY s.paidDate
            """)
    List<Object[]> sumByDay(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
