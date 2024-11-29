package com.example.digitalplatform.repository;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RatingParametersTest {

    @Autowired
    RatingParametersRepository ratingParametersRepository;

    @Test
    void success() {

        List<RatingParameters> subjectAreas = ratingParametersRepository.findAll();
        assertEquals(5,subjectAreas.size());

    }

}
