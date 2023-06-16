package com.example.demo.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {

    //    @Autowired
    @Qualifier("customAuthenticationEntryPoint")
    AuthenticationEntryPoint authEntryPoint;
    private final String path = "/api/v1/auth";
    private final String[] AUTH_WHITELIST = {
//            path +"/login/**", path + "/forgot-password", path + "/change-password",
//            path + "/register", path + "/verify-code", path + "/references",
            "/v3/api-docs/**",  "/configuration/**",   "/swagger*/**",
            "/swagger-ui/**",  "/webjars/**", "/api/v1/wallet/validate-account",

            path + "/register", path + "/verify-code", path + "/references",
            path + "/forgot-password/**",
            path + "/reset-password/**",
            path + "/verify-token/**",
            path + "/complete-registration/**",
            path + "/admin/sign-up",
            path + "/admin/login",
            path + "/admin/forgot-password",
            path + "/admin/reset-password/**",
            path + "/admin/complete-registration",
            path + "/sign-up",
            path + "/login",
            path + "/forgot-password",
            path + "/reset-password/**",
            path + "/complete-registration",
            path + "/staff/sign-up",
            path + "/staff/login",
            path + "/staff/complete-staff-registration",
            path + "/staff/login",
            path + "/staff/forgot-password",
            path + "/staff/reset-password"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailService userDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest()
                .authenticated()
                .and().exceptionHandling().authenticationEntryPoint(authEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}