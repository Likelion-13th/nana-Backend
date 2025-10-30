package likelion13th.shop.DTO.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemRequest {
    private String item_name;
    private int item_price;
    private int item_quantity;
    private String item_brand;
    private String imagePath;
    private boolean isNewItem;
    private List<Long> categoryIds;

}
