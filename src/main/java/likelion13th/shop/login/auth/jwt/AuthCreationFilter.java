package likelion13th.shop.login.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCreationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 현재 로직: /users/reissue 에서만 동작시키고 나머지는 모두 스킵
        // 필요 시 다른 리프레시 엔드포인트도 추가 가능
        String uri = request.getRequestURI();
        if (uri == null) return true;

        // [ADD] 프리플라이트 무조건 패스
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        // [FIX] 명시적으로 "동작시킬 경로"만 허용 (그 외는 스킵)
        boolean enabled = "/users/reissue".equals(uri)
                // || uri.startsWith("/auth/refresh")    // 필요시 주석 해제
                ;
        if (!enabled) {
            // [ADD] 디버깅 로그
            log.debug("[AuthCreationFilter] SKIP uri={}", uri);
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication existing = SecurityContextHolder.getContext().getAuthentication(); // [FIX] 변수명 오타 수정
        if (existing != null && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // [FIX] "Bearer " 공백 포함 길이 7
        String token = authHeader.substring(7);

        String providerId;
        try {
            Claims claims = tokenProvider.parseClaimsAllowExpired(token);
            providerId = claims.getSubject();
        } catch (Exception e) {
            log.debug("[AuthCreationFilter] token parse failed: {}", e.toString()); // [ADD]
            filterChain.doFilter(request, response);
            return;
        }

        if (providerId == null || providerId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        var anonymousAuthorities = java.util.Collections.singletonList(
                // [FIX] ROLE 접두어 및 이상한 기호 제거
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS")
        );

        var preAuth = new org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken(
                providerId, "N/A", anonymousAuthorities
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(preAuth);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }
}
