package likelion13th.shop.service;

import jakarta.transaction.Transactional;
import likelion13th.shop.DTO.request.OrderCreateRequest;
import likelion13th.shop.DTO.response.ItemResponseDto;
import likelion13th.shop.DTO.response.OrderResponseDto;
import likelion13th.shop.domain.Category;
import likelion13th.shop.domain.Item;
import likelion13th.shop.domain.Order;
import likelion13th.shop.domain.User;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.constant.OrderStatus;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.repository.CategoryRepository;
import likelion13th.shop.repository.ItemRepository;
import likelion13th.shop.repository.OrderRepository;
import likelion13th.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public List<ItemResponseDto> getItemsByCategoryId(Long categoryId){
        Category category  = categoryRepository.findById(categoryId).orElseThrow(()->new GeneralException(ErrorCode.CATEGORY_NOT_FOUND));
        return category.getItems().stream().map(ItemResponseDto::from).collect(Collectors.toList());
    }
}

// 카테고리 ID로 해당 카테고리에 속한 아이템들 리스트로 가져오는 서비스임
// 예외 처리해서 잘못된 카테고리 접근 막고 응답용 DTO로 변환해줌