package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
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

    private String code;

    private String description;
    @Column(name = "min_value")
    private float minValue;
    @Column(name = "max_value")
    private float maxValue;
    private float coefficient;


}
