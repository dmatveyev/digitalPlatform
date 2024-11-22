package com.example.digitalplatform.core.dessision;

import com.example.digitalplatform.core.BackpackBellman;
import com.example.digitalplatform.service.DecisionService;
import com.example.digitalplatform.service.RequestService;
import com.example.digitalplatform.service.UserService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DecisionService.class)
public class DecisionGeneratorConfig {

    @MockBean
    UserService userService;
    @MockBean
    RequestService requestService;

    @Bean
    BackpackBellman backpackBellman() {
        return new BackpackBellman();
    }

}
