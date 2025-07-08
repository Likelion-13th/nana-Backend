package likelion13th.shop.domain;


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
@Table(name = "Category") //예약어 회피
@NoArgsConstructor

public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false)
    private String category_name;

    @ManyToMany(mappedBy = "catefories")
    private List<Item> items = new ArrayList<>();

    public Category(String category_name) {
        this.category_name = category_name;
    }
}
