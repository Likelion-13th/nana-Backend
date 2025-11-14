package likelion13th.shop.login.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion13th.shop.domain.Address;
import likelion13th.shop.domain.User;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.auth.dto.JwtDto;
import likelion13th.shop.login.auth.jwt.CustomUserDetails;
import likelion13th.shop.login.auth.service.JpaUserDetailsManager;
import likelion13th.shop.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JpaUserDetailsManager jpaUserDetailsManager;
    private final UserService userService;

    // 현재는 사용하지 않지만, 필요하면 추가 검증용으로 유지
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "https://nana-frontend.netlify.app"
    );

    private static final String DEFAULT_FRONT = "https://nana-frontend.netlify.app";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            // 1. providerId, nickname 추출
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String providerId = String.valueOf(
                    oAuth2User.getAttributes().getOrDefault("provider_id",
                            oAuth2User.getAttributes().get("id"))
            );
            String nickname = (String) oAuth2User.getAttributes().get("nickname");

            log.info("OAuth2 Success providerId={}, nickname={}", providerId, nickname);

            // 2. 신규 회원 생성
            if (!jpaUserDetailsManager.userExists(providerId)) {
                User newUser = User.builder()
                        .providerId(providerId)
                        .usernickname(nickname)
                        .deletable(true)
                        .build();

                newUser.setAddress(new Address("10540", "경기도 고양시 덕양구 항공대학로 76", "한국항공대학교"));

                jpaUserDetailsManager.createUser(new CustomUserDetails(newUser));
                log.info("신규 회원 생성 완료 = {}", providerId);
            }

            // 3. JWT 발급
            JwtDto jwt = userService.jwtMakeSave(providerId);

            // 4. 세션에서 redirect_uri 가져오기
            //    ★ getSession(false) 로 "기존 세션만" 조회 (새 세션 생성 방지)
            var session = request.getSession(false);
            String redirectOrigin = null;
            String sessionId = null;

            if (session != null) {
                sessionId = session.getId();
                redirectOrigin = (String) session.getAttribute("FRONT_REDIRECT_URI");
                session.removeAttribute("FRONT_REDIRECT_URI");
            } else {
                log.info("[OAuth2SuccessHandler] session is null (no existing session)");
            }

            // ★ 추가 로그: 세션ID와 redirectOrigin 값 확인
            log.info("[OAuth2SuccessHandler] sessionId={}, redirectOrigin={}", sessionId, redirectOrigin);

            // 5. 유효성 검사: 세션에 값이 없을 때만 기본 프론트로
            if (redirectOrigin == null || redirectOrigin.isBlank()) {
                log.info("[OAuth2SuccessHandler] redirectOrigin is null/blank, fallback to DEFAULT_FRONT={}", DEFAULT_FRONT);
                redirectOrigin = DEFAULT_FRONT;
            }


            // 6. 최종 URL 생성
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(redirectOrigin)
                    .queryParam("accessToken", URLEncoder.encode(jwt.getAccessToken(), StandardCharsets.UTF_8))
                    .build(true)
                    .toUriString();

            log.info("최종 리다이렉트 = {}", redirectUrl);

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2SuccessHandler Error: {}", e.getMessage());
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
