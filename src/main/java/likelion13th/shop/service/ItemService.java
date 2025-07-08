package likelion13th.shop.service;

import jakarta.transaction.Transactional;
import likelion13th.shop.DTO.request.OrderCreateRequest;
import likelion13th.shop.DTO.response.ItemResponseDto;
import likelion13th.shop.DTO.response.OrderResponseDto;
import likelion13th.shop.domain.Item;
import likelion13th.shop.global.constant.OrderStatus;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }
}
