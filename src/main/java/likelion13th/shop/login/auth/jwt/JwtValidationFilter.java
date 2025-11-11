package likelion13th.shop.login.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.net.MalformedURLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    /**
     * 특정 경로는 JWT 검증을 건너뛴다.
     * (로그아웃/재발급/스웨거/OAuth 등)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return false;

        return uri.startsWith("/users/reissue")   // 토큰 재발급
                || uri.startsWith("/users/logout") // 로그아웃
                || uri.startsWith("/oauth2")       // OAuth2 시작
                || uri.startsWith("/login/oauth2") // OAuth2 콜백
                || uri.startsWith("/swagger-ui")   // Swagger UI
                || uri.startsWith("/v3/api-docs")  // Swagger Docs
                || uri.startsWith("/health");      // 헬스 체크
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증이 설정되어 있으면 그대로 진행
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated() && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = tokenProvider.parseClaims(token);

            String providerId = claims.getSubject();
            if (providerId == null || providerId.isEmpty()) {
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            }

            var authorities = tokenProvider.getAuthFromClaims(claims);

            CustomUserDetails userDetails = new CustomUserDetails(
                    providerId,
                    "",
                    authorities
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.security.SecurityException | MalformedURLException e) {
            // 잘못된 서명/형식
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            // 토큰 만료
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 형식
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            // 널/공백 등 잘못된 입력
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            // 예기치 못한 오류
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // 기존 정책 유지: 401 반환
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.onFailure(errorCode, null))
        );
    }
}
