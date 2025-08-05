package likelion13th.shop.service;

import likelion13th.shop.DTO.response.AddressResponse;
import likelion13th.shop.DTO.response.UserInfoResponse;
import likelion13th.shop.DTO.response.UserMileageResponse;
import likelion13th.shop.domain.Address;
import likelion13th.shop.domain.User;
import likelion13th.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserRepository userRepository;

    // 가짜 로그인 유저 가져오기 (id=1로 고정)
    private User getCurrentUser() {
        return userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
    }

    public List<AddressResponse> getUserAddresses() {
        Address address = getCurrentUser().getAddress(); // ✅ 유저에서 바로 가져옴
        return List.of(AddressResponse.from(address));   // ✅ 리스트로 감싸서 반환
    }

    public UserInfoResponse getUserProfile() {
        return UserInfoResponse.from(getCurrentUser());
    }

    public UserMileageResponse getUserMileage() {
        return new UserMileageResponse(getCurrentUser().getMaxMileage());
    }
}

// 유저 주소, 프로필, 마일리지 조회하는 서비스
// 유저 엔티티 안의 address 필드 직접 꺼내서 사용함
