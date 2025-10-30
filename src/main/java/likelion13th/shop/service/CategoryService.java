package likelion13th.shop.service;

import likelion13th.shop.DTO.response.ItemResponse;
import likelion13th.shop.domain.Category;
import likelion13th.shop.domain.Item;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /** 카테고리 존재 여부 확인 **/
    // 이런 식으로 검증하는 메서드를 따로 만들어서 재사용성 높일 수 있음
    public Category findCategoryById(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(()-> new GeneralException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /** 카테고리 별 상품 목록 조회 **/
    // DTO에 담아서 반환
    public List<ItemResponse> getItemsByCategory(Long categoryId) {
        // 카테고리 유효성 검사
        Category category = findCategoryById(categoryId);

        List<Item> items = category.getItems();
        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
}

// 카테고리 ID로 해당 카테고리에 속한 아이템들 리스트로 가져오는 서비스임
// 예외 처리해서 잘못된 카테고리 접근 막고 응답용 DTO로 변환해줌