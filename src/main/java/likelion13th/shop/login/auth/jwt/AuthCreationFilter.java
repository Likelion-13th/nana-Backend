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
        String uri = request.getRequestURI();
        if (uri == null) return true;

        // ✅ 프리플라이트 무조건 패스
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        // ✅ /users/reissue만 실행, 나머지는 스킵
        boolean enabled = "/users/reissue".equals(uri);
        if (!enabled) {
            log.debug("[AuthCreationFilter] SKIP uri={}", uri);
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
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

        String token = authHeader.substring(7); // ✅ Bearer 공백 포함

        String providerId;
        try {
            Claims claims = tokenProvider.parseClaimsAllowExpired(token);
            providerId = claims.getSubject();
        } catch (Exception e) {
            log.debug("[AuthCreationFilter] token parse failed: {}", e.toString());
            filterChain.doFilter(request, response);
            return;
        }

        if (providerId == null || providerId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        var anonymousAuthorities = java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS") // ✅ 수정
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
