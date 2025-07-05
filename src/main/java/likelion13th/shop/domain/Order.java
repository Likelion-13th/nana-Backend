package likelion13th.shop.domain;

import jakarta.persistence.*;
import likelion13th.shop.domain.entity.BaseEntity;
import likelion13th.shop.global.constant.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity //DB 한 줄
@Getter //get함수 항상 쓰기 귀찮으니 전역 딸깍
@Table(name = "orders") //예약어 회피
@NoArgsConstructor
//파라미터가 없는 디폴트 생성자 자동으로 생성
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //유일하게 생성해라. 중복 없이
    @Column(name = "order_id") //컬럼명
    @Setter(AccessLevel.PRIVATE) //PK키가 바뀌면 안되기 때문에 프라이빗 걸어둠
    private Long id; //변수명 = id, 자료형 = Long(데이터가 쥰내 많을 수 있기에)

    @Column(nullable = false)
    private int quantity; //필요한 요소

    @Column(nullable = false)
    @Setter
    private int totalPrice; //기존 주문 내역을 유지하기 위해

    @Column(nullable = false)
    @Setter
    private int finalPrice; //필요한 요소

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status; //얘도 요소

    //Item, User 와 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY) //1대다!!!!
    @JoinColumn(name = "item_id") //관계 맺 (핑크 열쇠)!!
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    //생성자 -> 객체 생성될 때 자동으로 실행! 즉 초기 설정을 할 때 사용
    public Order(User user, Item item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }
        //a.user = user -> a 통 안에 있는 퀀티티,프라이스,등등...에다가 밖에서 받아온 인자들을 넣음?각각?
        this.user = user; //오른쪽 유저는 밖에서 인자로 받아온 거, this 통에 있는 퀀티티..어쩌구들한테 던져줌
        this.item = item; //this가 통이고, 긁적긁적긁..적......
        this.quantity = quantity;
        this.status = OrderStatus.PROCESSING;
        this.totalPrice = item.getPrice() * quantity;

        // 연관관계 편의 메서드 호출
        user.getOrders().add(this);
        item.getOrders().add(this);
    }

    // 주문 상태 업데이트
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }


    //양방향 편의 메서드
    public void setUser(User user) {
        this.user = user;
        if (!user.getOrders().contains(this)) {
            user.getOrders().add(this);
        } // 반대쪽 객체에도 연관관계를 설정
    }

    public void setItem(Item item) {
        this.item = item;
        if (!item.getOrders().contains(this)) {
            item.getOrders().add(this);
        }
    }
}
