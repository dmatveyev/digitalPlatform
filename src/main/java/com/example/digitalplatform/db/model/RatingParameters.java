package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "RATING_PARAMETERS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "'code' must not be null, empty or blank")
    private String code;

    @NotBlank(message = "'description' must not be null, empty or blank")
    private String description;
    @Column(name = "min_value")
    private float minValue;
    @Column(name = "max_value")
    private float maxValue;
    private float coefficient;


}
