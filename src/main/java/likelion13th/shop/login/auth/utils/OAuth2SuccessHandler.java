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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JpaUserDetailsManager jpaUserDetailsManager;
    private final UserService userService;

    // ✅ 환경에서 주입: 로컬이면 localhost:3000, 배포면 nana-frontend.netlify.app
    // 기본값을 nana-frontend로 설정 (설정 없으면 이걸 씀)
    @Value("${app.front-url:https://nana-frontend.netlify.app}")
    private String frontUrl;


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

            // 4. 프론트 리다이렉트 URL = 환경에서 받은 frontUrl 하나만 사용
            String target = (frontUrl == null || frontUrl.isBlank())
                    ? "https://nana-frontend.netlify.app"
                    : frontUrl;

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(target)
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
