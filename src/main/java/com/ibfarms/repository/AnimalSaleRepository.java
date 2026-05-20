package com.ibfarms.repository;

import com.ibfarms.entity.AnimalSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalSaleRepository extends JpaRepository<AnimalSale, Long> {

    Optional<AnimalSale> findByAnimalId(Long animalId);

    @Query("""
            SELECT s FROM AnimalSale s
            WHERE s.animal.owner.id = :ownerId
              AND s.saleDate BETWEEN :from AND :to
            ORDER BY s.saleDate DESC
            """)
    List<AnimalSale> findByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(s.salePrice), 0) FROM AnimalSale s
            WHERE s.animal.owner.id = :ownerId
              AND s.saleDate BETWEEN :from AND :to
            """)
    BigDecimal sumSalePriceByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
