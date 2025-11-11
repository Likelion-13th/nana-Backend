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
import org.springframework.http.HttpMethod; // ★ added
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
                        // ★ added: 프리플라이트(OPTIONS) 전부 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/users/reissue",
                                "/users/logout",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/categories/**",
                                "/items/**",
                                // ★ added: 스웨거에서 토큰 발급 테스트 시 401/403 방지
                                "/token/**"
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
        CorsConfiguration config = new CorsConfiguration();

        // 브라우저 쿠키/인증정보 포함 허용
        config.setAllowCredentials(true);

        // ★ changed: setAllowedOriginPatterns 사용 (credentials=true와 호환되며 와일드카드/서브도메인 허용)
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000", // 로컬 프론트
                "http://localhost:8080", // 로컬 스웨거
                // EB 스웨거/API (HTTP)
                "http://sajang-dev-env.eba-cxzcfs22.ap-northeast-2.elasticbeanstalk.com",
                // 배포 프론트 (넷리파이 계열 전반 허용; 필요 시 정확한 도메인으로 좁히세요)
                "https://*.netlify.app",
                // 선택: 커스텀 도메인 사용 중이면 추가 (없으면 제거 가능)
                "https://valuebid.site"
        ));

        // 허용 헤더/메서드 넉넉히 개방
        config.addAllowedHeader("*"); // Authorization, Content-Type 포함
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 노출 헤더(필요 시)
        config.setExposedHeaders(Arrays.asList("Authorization", "Location", "Link"));

        // 프리플라이트 캐시
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
