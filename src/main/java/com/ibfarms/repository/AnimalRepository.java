package com.ibfarms.repository;

import com.ibfarms.entity.Animal;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    Optional<Animal> findByIdAndOwner(Long id, User owner);

    boolean existsByTagNumberAndOwner(String tagNumber, User owner);

    boolean existsByTagNumberAndOwnerAndIdNot(String tagNumber, User owner, Long id);

    long countByOwnerAndStatus(User owner, AnimalStatus status);

    long countByOwnerAndPregnantTrueAndStatus(User owner, AnimalStatus status);

    @Query("""
            SELECT a FROM Animal a
            WHERE a.owner = :owner
              AND (:search IS NULL OR :search = '' OR
                   LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(a.tagNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(a.breed) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:species IS NULL OR :species = '' OR a.species = :species)
              AND (:status IS NULL OR a.status = :status)
              AND (:pregnant IS NULL OR a.pregnant = :pregnant)
            """)
    Page<Animal> search(
            @Param("owner") User owner,
            @Param("search") String search,
            @Param("species") String species,
            @Param("status") AnimalStatus status,
            @Param("pregnant") Boolean pregnant,
            Pageable pageable);

    List<Animal> findByOwnerAndPregnantTrueAndStatusOrderByExpectedDeliveryDateAsc(
            User owner, AnimalStatus status);

    @Query("SELECT DISTINCT a.species FROM Animal a WHERE a.owner = :owner ORDER BY a.species")
    List<String> findDistinctSpeciesByOwner(@Param("owner") User owner);
}
