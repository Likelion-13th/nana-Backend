package likelion13th.shop.global.config;

import likelion13th.shop.login.auth.jwt.AuthCreationFilter;
import likelion13th.shop.login.auth.jwt.JwtValidationFilter;
import likelion13th.shop.login.auth.handler.OAuth2SuccessHandler;
import likelion13th.shop.login.auth.utils.OAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.security.config.Customizer.withDefaults;

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
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .anonymous(withDefaults()) // ✅ 익명 접근 보장

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",                // ✅ 루트 허용
                                "/favicon.ico",     // ✅ 파비콘 허용
                                "/error",           // ✅ 에러 페이지 허용
                                "/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/users/reissue",
                                "/users/logout",
                                "/oauth2/**",              // ✅ OAuth2 시작/콜백 허용
                                "/oauth2/authorization/**",// ✅ 추가 허용
                                "/login/oauth2/**",
                                "/categories/**",
                                "/items/**",
                                "/token/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(ep -> ep.baseUri("/oauth2/start"))
                        .redirectionEndpoint(ep -> ep.baseUri("/login/oauth2/code/*"))
                        .successHandler(oAuth2SuccessHandler)
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                )

                // ✅ 필터 순서 유지
                .addFilterBefore(authCreationFilter, AnonymousAuthenticationFilter.class)
                .addFilterBefore(jwtValidationFilter, AuthCreationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://sajang-dev-env.eba-cxzcfs22.ap-northeast-2.elasticbeanstalk.com",
                "https://*.netlify.app",
                "https://valuebid.site"
        ));
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Location", "Link"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
