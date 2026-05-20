package com.ibfarms.repository;

import com.ibfarms.entity.OtherExpense;
import com.ibfarms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OtherExpenseRepository extends JpaRepository<OtherExpense, Long> {

    List<OtherExpense> findByOwnerOrderByExpenseDateDesc(User owner);

    Optional<OtherExpense> findByIdAndOwner(Long id, User owner);

    @Query("""
            SELECT o FROM OtherExpense o
            WHERE o.owner.id = :ownerId
              AND o.expenseDate BETWEEN :from AND :to
            ORDER BY o.expenseDate DESC
            """)
    List<OtherExpense> findByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(o.amount), 0) FROM OtherExpense o
            WHERE o.owner.id = :ownerId
              AND o.expenseDate BETWEEN :from AND :to
            """)
    BigDecimal sumByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT o.expenseDate, SUM(o.amount) FROM OtherExpense o
            WHERE o.owner.id = :ownerId
              AND o.expenseDate BETWEEN :from AND :to
            GROUP BY o.expenseDate
            ORDER BY o.expenseDate
            """)
    List<Object[]> sumByDay(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
