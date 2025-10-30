package likelion13th.shop.login.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import likelion13th.shop.domain.User;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.auth.dto.JwtDto;
import likelion13th.shop.login.auth.jwt.RefreshToken;
import likelion13th.shop.login.auth.jwt.TokenProvider;
import likelion13th.shop.login.auth.repository.RefreshTokenRepository;
import likelion13th.shop.login.auth.service.JpaUserDetailsManager;
import likelion13th.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final JpaUserDetailsManager manager;

    public Boolean chechMemberByProviderId(String providerId) {
        return userRepository.existsByProviderId(providerId);
    }

    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }
    //조회
    public User getAuthenticatedUser(String providerId){
        return userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void saveRefreshToken(String providerId, String refreshToken){
        //사용자 조회해오기
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        RefreshToken token = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.updateRefreshToken(refreshToken);
                    return existingToken;
                })
                .orElseGet(() -> {
                    return RefreshToken.builder()
                            .user(user)
                            .refreshToken(refreshToken)
                            .ttl(System.currentTimeMillis() + 1000L * 60 * 60 * 24 *7)
                            .build();
                });
        refreshTokenRepository.save(token);
    }

    @Transactional
    public JwtDto jwtMakeSave(String providerId){
        //providerId -> 사용자 로드
        UserDetails details = manager.loadUserByUsername(providerId);

        //JWT 만들기
        JwtDto jwt = tokenProvider.generateTokens(details);

        saveRefreshToken(providerId, jwt.getRefreshToken());
        return jwt;
    }

    @Transactional
    public JwtDto reissue(HttpServletRequest request){
        String accessToken = request.getHeader("Authorization");
        if(accessToken != null && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        Claims claims;
        try{
            claims = tokenProvider.parseClaimsAllowExpired(accessToken);
        } catch(Exception e){
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }
        String providerId = claims.getSubject();

        if(providerId==null || providerId.isEmpty()){
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        User user = findByProviderId(providerId)
                .orElseThrow(() -> {
                    return new GeneralException(ErrorCode.USER_NOT_FOUND);
                });

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));

        if (!tokenProvider.validateToken(refreshToken.getRefreshToken())){
            refreshTokenRepository.deleteByUser(user);
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        UserDetails userDetails = manager.loadUserByUsername(providerId);
        JwtDto newJwt = tokenProvider.generateTokens(userDetails);

        refreshToken.updateRefreshToken(newJwt.getRefreshToken());
        refreshTokenRepository.save(refreshToken);

        return newJwt;
    }

    @Transactional
    public void logout(HttpServletRequest request){
        String accessToken = request.getHeader("Authorization");
        if(accessToken != null && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        Claims claims = tokenProvider.parseClaims(accessToken);
        String providerId = claims.getSubject();
        if ((providerId != null || providerId.isEmpty())) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        User user = findByProviderId(providerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
    }
}