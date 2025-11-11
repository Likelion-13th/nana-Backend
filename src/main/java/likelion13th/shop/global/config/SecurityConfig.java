package likelion13th.shop.global.config;

import likelion13th.shop.login.auth.jwt.AuthCreationFilter;
import likelion13th.shop.login.auth.jwt.JwtValidationFilter;
import likelion13th.shop.login.auth.utils.OAuth2SuccessHandler;
import likelion13th.shop.login.auth.utils.OAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final AuthCreationFilter authCreationFilter;
    private final JwtValidationFilter jwtValidationFilter;
    private final OAuth2UserServiceImpl oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/users/reissue",
                                "/users/logout",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/categories/**",
                                "/items/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 세션 비활성화 (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // OAuth2 로그인
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                )

                // 필터 체인
                .addFilterBefore(authCreationFilter, AnonymousAuthenticationFilter.class)
                .addFilterBefore(jwtValidationFilter, AuthCreationFilter.class);

        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://nana-frontend.netlify.app/",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
