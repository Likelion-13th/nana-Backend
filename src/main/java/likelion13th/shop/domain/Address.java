package likelion13th.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@Getter
public class Address {

    @Column(nullable = false)
    private String zipcode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String addressDetail;

    public Address() {
        this.zipcode = "10540";
        this.address = "경기도 고양시 덕양구 항공대학로 76";
        this.addressDetail = "한국항공대학교";
    }
}

// 우편 번호, 주소, 상세 주소를 멤버로 갖는 객체를 따로 선언한 파일임
// 주소가 유저 테이블 안에 들어가 있는 거라서 관계 코드가 없음