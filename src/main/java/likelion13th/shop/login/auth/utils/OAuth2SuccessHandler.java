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

                newUser.setAddress(new Address("10540","경기도 고양시 덕양구 항공대학로 76","한국항공대학교"));

                jpaUserDetailsManager.createUser(new CustomUserDetails(newUser));
                log.info("신규 회원 생성 완료 = {}", providerId);
            }

            // 3. JWT 발급
            JwtDto jwt = userService.jwtMakeSave(providerId);

            // 4. 세션에서 redirect_uri 가져오기
            String redirectOrigin = (String) request.getSession().getAttribute("FRONT_REDIRECT_URI");
            request.getSession().removeAttribute("FRONT_REDIRECT_URI");

            // 5. 유효성 검사
            if (redirectOrigin == null || !ALLOWED_ORIGINS.contains(redirectOrigin)) {
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
