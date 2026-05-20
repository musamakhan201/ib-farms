package com.ibfarms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "growth_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Column(precision = 8, scale = 2)
    private BigDecimal lengthCm;

    @Column(precision = 10, scale = 2)
    private BigDecimal weightKg;

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
}
