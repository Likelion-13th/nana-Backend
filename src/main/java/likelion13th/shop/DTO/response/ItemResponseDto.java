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
