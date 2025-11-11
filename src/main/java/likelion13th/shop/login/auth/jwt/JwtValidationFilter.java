package likelion13th.shop.login.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return false;

        // ✅ 토큰 검증을 생략할(=비인증) 경로들만 예외로 둔다.
        return uri.contains("/users/reissue")
                || uri.contains("/oauth2")
                || uri.contains("/login/oauth2")
                || uri.contains("/swagger-ui")
                || uri.contains("/v3/api-docs")
                || uri.contains("/health");
        // ⛔ 로그아웃은 인증 경로이므로 제외하지 않는다.
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("[JwtValidationFilter] 요청 URI = {}", uri);

        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JwtValidationFilter] Authorization 헤더가 없음 또는 잘못된 형식. uri={}", uri);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = tokenProvider.parseClaims(token);
            String providerId = claims.getSubject();

            if (providerId == null || providerId.isEmpty()) {
                log.warn("[JwtValidationFilter] providerId가 비어 있음. uri={}", uri);
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            }

            var authorities = tokenProvider.getAuthFromClaims(claims);
            CustomUserDetails userDetails = new CustomUserDetails(providerId, "", authorities);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("[JwtValidationFilter] 토큰 검증 성공: providerId={}, 권한={}", providerId, authorities);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("[JwtValidationFilter] 토큰 만료: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("[JwtValidationFilter] 잘못된 서명: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (MalformedJwtException e) {
            log.error("[JwtValidationFilter] JWT 형식 오류: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e) {
            log.error("[JwtValidationFilter] 지원하지 않는 JWT 형식: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.error("[JwtValidationFilter] 잘못된 인자/공백 토큰: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("[JwtValidationFilter] 알 수 없는 오류 발생: {}", e.getMessage(), e);
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
