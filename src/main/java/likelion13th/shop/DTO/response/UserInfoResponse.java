package likelion13th.shop.DTO.response;

import likelion13th.shop.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getUsernickname())
                .email("")
                .build();
    }

}

// 유저 아이디랑 닉네임, 이메일 담는 응답용 DTO임
// 이메일은 아직 데이터 없어서 빈 문자열로 처리함
