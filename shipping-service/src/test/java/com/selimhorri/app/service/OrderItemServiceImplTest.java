package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.impl.OrderItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    @DisplayName("findAll enriches each order item with product and order")
    void findAllEnrichesRelationships() {
        OrderItem orderItem = sampleOrderItem();
        when(orderItemRepository.findAll()).thenReturn(List.of(orderItem));
        when(restTemplate.getForObject(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + 1, ProductDto.class))
            .thenReturn(ProductDto.builder().productId(1).productTitle("Phone").build());
        when(restTemplate.getForObject(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + 10, OrderDto.class))
            .thenReturn(OrderDto.builder().orderId(10).orderDesc("Order").build());

        List<OrderItemDto> result = orderItemService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductDto().getProductTitle()).isEqualTo("Phone");
        assertThat(result.get(0).getOrderDto().getOrderDesc()).isEqualTo("Order");
    }

    @Test
    @DisplayName("findById throws when repository returns empty")
    void findByIdThrowsWhenMissing() {
        when(orderItemRepository.findById(any())).thenReturn(Optional.empty());

        OrderItemId id = new OrderItemId(1, 10);

        assertThatThrownBy(() -> orderItemService.findById(id))
            .isInstanceOf(OrderItemNotFoundException.class);
    }

    @Test
    @DisplayName("deleteById delegates to repository")
    void deleteByIdDelegates() {
        OrderItemId id = new OrderItemId(1, 10);

        orderItemService.deleteById(id);

        verify(orderItemRepository).deleteById(id);
    }

    private OrderItem sampleOrderItem() {
        return OrderItem.builder()
            .productId(1)
            .orderId(10)
            .orderedQuantity(2)
            .build();
    }
}
