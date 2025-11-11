package likelion13th.shop.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.api.SuccessCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.auth.dto.JwtDto;
import likelion13th.shop.login.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 관련 API(토큰 재발급, 로그아웃) 입니다.")
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    // ✅ EB에서도 확실히 동작하도록 명시적 생성자 사용
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "토큰 재발급", description = "만료된 토큰을 재발급합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 이상해요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않아요")
    })
    @PostMapping("/reissue")
    public ApiResponse<JwtDto> reissue(HttpServletRequest request) {
        try {
            JwtDto jwt = userService.reissue(request);
            return ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, jwt);
        } catch (GeneralException e) {
            log.error("[UserController] 재발급 처리 중 GeneralException 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("[UserController] 재발급 처리 중 알 수 없는 오류", e);
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "로그아웃", description = "현재 사용자 토큰을 무효화하고 로그아웃합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 이상해요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않아요")
    })
    @DeleteMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        try {
            userService.logout(request);
            return ApiResponse.onSuccess(SuccessCode.USER_LOGOUT_SUCCESS, null);
        } catch (GeneralException e) {
            log.error("[UserController] 로그아웃 처리 중 GeneralException 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("[UserController] 로그아웃 처리 중 알 수 없는 오류", e);
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
