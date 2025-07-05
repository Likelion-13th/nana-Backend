package likelion13th.shop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import likelion13th.shop.domain.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity //DB 한 줄
@Getter //get함수 항상 쓰기 귀찮으니 전역 딸깍
@Table(name = "Item") //예약어 회피
@NoArgsConstructor
//파라미터가 없는 디폴트 생성자 자동으로 생성

public class Item extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false)
    private String item_name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String imagePath;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    @Setter
    private boolean isNew= false;

    //Category와 다대다 연관관계 설정
    @ManyToMany(mappedBy = "items") //다대다 원래 안되는데 알아서 잘 해줌 nice
    private List<Category> categories = new ArrayList<>();

    //Order과 일대다 연관관계 설정
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    public Item(String item_name, int price, String thumbnail_img, String brand, boolean isNew) {
        this.item_name = item_name;
        this.price = price;
        this.imagePath = imagePath;
        this.brand = brand;
        this.isNew= false;
    }
}