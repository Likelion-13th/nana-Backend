package likelion13th.shop.repository;

import likelion13th.shop.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface ItemRepository extends JpaRepository<Item,Long>{
    Optional<Item> findById(Long itemId); // 리스트에서 뭐 꺼낼 때 get 이렇게 꺼내오는데 이거랑 같은 원리로 객체처럼 움직임
}
