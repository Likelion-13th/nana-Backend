package likelion13th.shop.DTO.response;

import likelion13th.shop.domain.Item;
import likelion13th.shop.global.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ItemResponseDto {
    private Long item_id;
    private String item_name;
    private int price;
    private String imagePath;
    private String brand;
    private boolean isNew;

    public static ItemResponseDto from(Item item){
        return new ItemResponseDto(
                item.getId(),
                item.getItem_name(),
                item.getPrice(),
                item.getImagePath(),
                item.getBrand(),
                item.isNew()
        );
    }
}

// 아이템 정보 응답용 DTO임
// 아이템 id, 이름, 가격, 브랜드 등 보여주기 위해 만든 클래스