package likelion13th.shop.login.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Tag(name = "인증", description = "OAuth2 로그인 시작 API")
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class AuthController {

    // ✅ origin 형태로 통일
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "https://nana-frontend.netlify.app",
            "http://localhost:3000"
    );
    private static final String DEFAULT_FRONT_ORIGIN = "https://nana-frontend.netlify.app";

    @Operation(summary = "카카오 로그인 시작", description = "redirect_uri를 검증·저장 후 카카오 인가로 리다이렉트합니다.")
    @GetMapping("/start/kakao")
    public void startKakao(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name = "redirect_uri", required = false) String redirectUri
    ) throws IOException {
        // 허용 Origin만 통과(https?://host[:port])
        String safe = pickSafeOrigin(redirectUri, ALLOWED_ORIGINS, DEFAULT_FRONT_ORIGIN);

        // ★ 세션 대신 쿠키에 저장
        String encoded = URLEncoder.encode(safe, StandardCharsets.UTF_8);
        Cookie frontCookie = new Cookie("FRONT_REDIRECT_URI", encoded);
        frontCookie.setPath("/");          // 전체 경로에서 사용
        frontCookie.setHttpOnly(true);     // JS에서 안 건드려도 됨
        // 필요하면 HTTPS에서만 나가게 하려면 true, 지금은 http도 쓰니까 false 유지
        frontCookie.setSecure(false);

        response.addCookie(frontCookie);

        log.info("[startKakao] rawRedirectUri={}, safeOrigin={}", redirectUri, safe);

        // 시큐리티 기본 인가 엔드포인트로 넘김
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    private String pickSafeOrigin(String url, Set<String> allowed, String fallback) {
        try {
            if (url == null || url.isBlank()) return fallback;
            URI u = URI.create(url);
            String origin = u.getScheme() + "://" + u.getHost() + (u.getPort() == -1 ? "" : ":" + u.getPort());
            return allowed.contains(origin) ? origin : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
