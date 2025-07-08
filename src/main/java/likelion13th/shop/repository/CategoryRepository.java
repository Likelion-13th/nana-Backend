package likelion13th.shop.repository;

import likelion13th.shop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>{ //Long으로 PK를 찍어줌(도메인에서 Long으로 선언했으니..)
    Optional<Category> findById(Long categoryId);
}

//DB에서 categoryid로 카테고리 찾는 코드임!!