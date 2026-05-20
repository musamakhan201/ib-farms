package com.ibfarms.repository;

import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AnimalExpenseRepository extends JpaRepository<AnimalExpense, Long> {

    List<AnimalExpense> findByAnimalOrderByExpenseDateDesc(Animal animal);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AnimalExpense e WHERE e.animal = :animal")
    BigDecimal sumByAnimal(@Param("animal") Animal animal);

    @Query("""
            SELECT e FROM AnimalExpense e
            WHERE e.animal.owner.id = :ownerId
              AND e.expenseDate BETWEEN :from AND :to
            ORDER BY e.expenseDate DESC
            """)
    List<AnimalExpense> findByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM AnimalExpense e
            WHERE e.animal.owner.id = :ownerId
              AND e.expenseDate BETWEEN :from AND :to
            """)
    BigDecimal sumByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT e.category, SUM(e.amount) FROM AnimalExpense e
            WHERE e.animal.owner.id = :ownerId
              AND e.expenseDate BETWEEN :from AND :to
            GROUP BY e.category
            ORDER BY SUM(e.amount) DESC
            """)
    List<Object[]> sumByCategory(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT e.expenseDate, SUM(e.amount) FROM AnimalExpense e
            WHERE e.animal.owner.id = :ownerId
              AND e.expenseDate BETWEEN :from AND :to
            GROUP BY e.expenseDate
            ORDER BY e.expenseDate
            """)
    List<Object[]> sumByDay(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
