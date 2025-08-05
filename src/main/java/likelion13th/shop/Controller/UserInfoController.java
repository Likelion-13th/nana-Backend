package likelion13th.shop.controller;

import likelion13th.shop.DTO.response.UserInfoResponse;
import likelion13th.shop.DTO.response.UserMileageResponse;
import likelion13th.shop.DTO.response.AddressResponse;
import likelion13th.shop.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserInfoController {

    private final UserAddressService userAddressService;

    @GetMapping("/profile")
    public ResponseEntity<UserInfoResponse> getProfile() {
        return ResponseEntity.ok(userAddressService.getUserProfile());
    }

    @GetMapping("/mileage")
    public ResponseEntity<UserMileageResponse> getMileage() {
        return ResponseEntity.ok(userAddressService.getUserMileage());
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressResponse>> getAddresses() {
        return ResponseEntity.ok(userAddressService.getUserAddresses());
    }
}

// 유저 프로필, 마일리지, 주소 조회하는 API 모아놓은 파일임
// OrderController처럼 응답 통일시키고 서비스로 로직 넘겨서 정리함