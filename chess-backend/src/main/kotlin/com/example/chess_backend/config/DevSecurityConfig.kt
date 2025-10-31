package com.example.chess_backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@Profile("dev") // Only active in dev profile
class DevSecurityConfig {

    @Bean
    fun devSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll() // Allow all requests in dev mode
            }
            .csrf { csrf ->
                csrf.disable() // Disable CSRF in dev mode
            }
            .headers { headers ->
                headers.frameOptions { frameOptions ->
                    frameOptions.sameOrigin() // Allow H2 console frames
                }
            }

        return http.build()
    }
}