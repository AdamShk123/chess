package com.example.chess_backend.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@Profile("!dev") // Only active when NOT in dev profile
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuer: String

    @Value("\${spring.security.oauth2.resourceserver.jwt.audiences}")
    private lateinit var audience: String

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    // Allow H2 console access
                    .requestMatchers("/h2-console/**").permitAll()
                    // All API endpoints require authentication
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            // Disable CSRF for API (using JWT tokens)
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/api/**", "/h2-console/**")
            }
            // Allow H2 console frames
            .headers { headers ->
                headers.frameOptions { frameOptions ->
                    frameOptions.sameOrigin()
                }
            }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromIssuerLocation<JwtDecoder>(issuer)

        val audienceValidator = AudienceValidator(audience)
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
        val tokenValidator = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        (jwtDecoder as NimbusJwtDecoder).setJwtValidator(tokenValidator)

        return jwtDecoder
    }
}

// Custom validator to check the audience claim
class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {

    private val logger = LoggerFactory.getLogger(AudienceValidator::class.java)

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        return if (jwt.audience.contains(audience)) {
            OAuth2TokenValidatorResult.success()
        } else {
            logger.error("Audience validation failed - expected '$audience' but got ${jwt.audience}")
            OAuth2TokenValidatorResult.failure(
                OAuth2Error("invalid_token", "The required audience is missing", null)
            )
        }
    }
}