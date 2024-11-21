package com.example.digitalplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests()
                .requestMatchers(AntPathRequestMatcher
                        .antMatcher("/h2-console/**")
                ).permitAll()
                .requestMatchers(AntPathRequestMatcher
                        .antMatcher("/login*")
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin(formLogin -> formLogin
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureHandler(myAuthenticationFailureHandler())
                )
                .logout(request-> request
                        .deleteCookies("JSESSIONID")
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        )
                .csrf().ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                .and()
                .headers(headers -> headers.frameOptions().sameOrigin())
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
        return new MySimpleUrlAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler myAuthenticationFailureHandler(){
        return new MySimpleUrlAuthenticationFailureHandler();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return encoder;
    }

}