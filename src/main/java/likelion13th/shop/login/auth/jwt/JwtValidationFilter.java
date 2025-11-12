package likelion13th.shop.login.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private static final AntPathMatcher PATH = new AntPathMatcher();

    private static final String[] SKIP_PATHS = new String[] {
            "/",
            "/favicon.ico",       // ✅ 파비콘도 스킵
            "/error",
            "/oauth2/**",         // ✅ 로그인 시작/콜백
            "/login/oauth2/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/health"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return false;

        // ✅ 프리플라이트는 무조건 패스
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        for (String p : SKIP_PATHS) {
            if (PATH.match(p, uri)) {
                log.debug("[JwtValidationFilter] SKIP path: {}", uri);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();
        log.debug("[JwtValidationFilter] 요청 URI = {}", uri);

        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Bearer 공백 포함
        String token = authHeader.substring(7);

        try {
            Claims claims = tokenProvider.parseClaims(token);
            String providerId = claims.getSubject();

            if (providerId == null || providerId.isEmpty()) {
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            }

            var authorities = tokenProvider.getAuthFromClaims(claims);
            CustomUserDetails userDetails = new CustomUserDetails(providerId, "", authorities);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("[JwtValidationFilter] 토큰 검증 성공: providerId={}", providerId);

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("[JwtValidationFilter] JWT 오류: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.onFailure(errorCode, null))
        );
    }
}
