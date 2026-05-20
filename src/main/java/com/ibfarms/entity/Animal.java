package com.ibfarms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animals", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "tag_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String species;

    @Column(length = 80)
    private String breed;

    @Column(name = "tag_number", nullable = false, length = 40)
    private String tagNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    /** Cloudinary public ID (e.g. ib-farms/animals/uuid). */
    @Column(length = 255)
    private String pictureFilename;

    @Column(nullable = false)
    @Builder.Default
    private boolean pregnant = false;

    private LocalDate pregnancyDate;

    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AnimalStatus status = AnimalStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("recordDate DESC")
    @Builder.Default
    private List<GrowthRecord> growthRecords = new ArrayList<>();

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("expenseDate DESC")
    @Builder.Default
    private List<AnimalExpense> expenses = new ArrayList<>();

    @OneToOne(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnimalSale sale;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
