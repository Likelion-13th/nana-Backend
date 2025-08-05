package likelion13th.shop.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMileageResponse {
    private int mileage;
}

// 유저 마일리지만 따로 응답할 때 쓰는 DTO임