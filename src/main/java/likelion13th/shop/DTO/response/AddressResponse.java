package likelion13th.shop.DTO.response;

import likelion13th.shop.domain.Address;
import lombok.Getter;

@Getter
public class AddressResponse {
    private String zipcode;
    private String address;
    private String addressDetail;

    public AddressResponse(Address address) {
        this.zipcode = address.getZipcode();
        this.address = address.getAddress();
        this.addressDetail = address.getAddressDetail();
    }
}

// 유저 주소 정보 응답용 DTO임 (우편번호, 주소, 상세주소)
// Address 객체에서 값 꺼내서 변환함