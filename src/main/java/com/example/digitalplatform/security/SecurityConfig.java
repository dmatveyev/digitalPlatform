package com.example.digitalplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests()
                .requestMatchers(AntPathRequestMatcher
                        .antMatcher("/h2-console/**")
                ).permitAll()
                .requestMatchers(AntPathRequestMatcher
                        .antMatcher("/login*")
                ).permitAll()
                // Страницы ошибок должны быть доступны без аутентификации,
                // т.к. контейнер диспетчеризует на /error после любого сбоя (включая 401/403).
                .requestMatchers(AntPathRequestMatcher
                        .antMatcher("/error")
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin(form -> form
                        // Страница логина — обычный Spring MVC view: LoginController (@GetMapping /login)
                        // возвращает Thymeleaf-шаблон login.html. POST /login обрабатывает форму
                        // (по умолчанию loginProcessingUrl = URL страницы логина).
                        .loginPage("/login")
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureHandler(mySimpleUrlAuthenticationFailureHandler())
                )
                // authenticationEntryPoint() идёт ПОСЛЕ formLogin: при loginPage(String) formLogin
                // сам создаёт LoginUrlAuthenticationEntryPoint и перезаписывает entry point, установленный
                // до него. Назначая наш bean (setUseForward(false)) после formLogin, гарантируем, что он
                // будет финальным для неавторизованных запросов: ExceptionTranslationFilter редиректит
                // на /login с 302, а не отвечает 401/403.
                // ВАЖНО: anonymous() НЕ вызывается явно — по умолчанию (с Spring Security 3.x до 6.x)
                // AnonymousAuthenticationFilter ВКЛЮЧЁН и подставляет AnonymousAuthenticationToken
                // в SecurityContext, когда authentication == null. Это именно то, что нужно для редиректа:
                // FilterSecurityInterceptor бросает AccessDeniedException (аноним не прошёл
                // anyRequest().authenticated()), а ExceptionTranslationFilter через
                // AuthenticationTrustResolver.isAnonymous(authentication) определяет анонима и вызывает
                // authenticationEntryPoint -> 302 /login. Если бы anonymous() был выключен, контекст остался
                // бы пустым (null), isAnonymous(null) == false, и пользователь получил бы 403 AccessDeniedHandler
                // вместо редиректа на логин.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(loginEntryPoint()))
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
    public AuthenticationFailureHandler mySimpleUrlAuthenticationFailureHandler() {
        return new MySimpleUrlAuthenticationFailureHandler();
    }

    /**
     * Неавторизованный запрос -> 302 редирект на /login (а не 403).
     * setUseForward(false) гарантирует именно HTTP-редирект, а не forward.
     */
    @Bean
    public LoginUrlAuthenticationEntryPoint loginEntryPoint() {
        LoginUrlAuthenticationEntryPoint entryPoint = new LoginUrlAuthenticationEntryPoint("/login");
        entryPoint.setUseForward(false);
        return entryPoint;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return encoder;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        String hierarchy = """
                ADMIN > TEACHER
                TEACHER > USER
                ADMIN > STUDENT
                STUDENT > USER""";
        RoleHierarchyImpl roleHierarchy = RoleHierarchyImpl.fromHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public DefaultWebSecurityExpressionHandler customWebSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

}