package com.ibfarms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AnimalFormDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Species is required")
    @Size(max = 50)
    private String species;

    @Size(max = 80)
    private String breed;

    @NotBlank(message = "Tag number is required")
    @Size(max = 40)
    private String tagNumber;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero")
    private BigDecimal purchasePrice;

    @NotNull(message = "Purchase date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;

    private boolean pregnant;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pregnancyDate;

    private String existingPictureFilename;

    /** Optional initial growth record when adding a new animal. */
    private boolean recordInitialGrowth;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate growthRecordDate;

    @DecimalMin(value = "0.0", message = "Height must be non-negative")
    private BigDecimal growthHeightCm;

    @DecimalMin(value = "0.0", message = "Length must be non-negative")
    private BigDecimal growthLengthCm;

    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal growthWeightKg;

    private String growthNotes;
}
