package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("findAll maps entities into immutable DTO list")
    void findAllReturnsImmutableList() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder()));

        List<OrderDto> result = orderService.findAll();

        assertThat(result).hasSize(1);
        @SuppressWarnings("unchecked")
        List<OrderDto> mutableView = (List<OrderDto>) (List<?>) result;
        OrderDto newOrder = OrderDto.builder().build();
        assertThatThrownBy(() -> mutableView.add(newOrder))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("update order delegates to repository with mapped DTO")
    void updateDelegatesToRepository() {
        Order order = sampleOrder();
        when(orderRepository.save(order)).thenReturn(order);

        OrderDto dto = orderService.update(OrderDto.builder()
            .orderId(order.getOrderId())
            .orderDate(order.getOrderDate())
            .orderDesc(order.getOrderDesc())
            .orderFee(order.getOrderFee())
            .cartDto(CartDto.builder().cartId(order.getCart().getCartId()).build())
            .build());

        verify(orderRepository).save(order);
        assertThat(dto.getOrderFee()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("update by id reuses existing entity")
    void updateByIdUsesExisting() {
        Order order = sampleOrder();
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDto updated = orderService.update(7, OrderDto.builder()
            .orderId(7)
            .orderFee(50.0)
            .cartDto(CartDto.builder().cartId(4).build())
            .build());

        verify(orderRepository).findById(7);
        verify(orderRepository).save(order);
        assertThat(updated.getOrderFee()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("findById returns order when exists")
    void findByIdReturnsOrder() {
        Order order = sampleOrder();
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        OrderDto result = orderService.findById(7);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(7);
        verify(orderRepository).findById(7);
    }

    @Test
    @DisplayName("findById throws when entity missing")
    void findByIdMissingThrows() {
        when(orderRepository.findById(100)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(100))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("save creates new order")
    void saveCreatesOrder() {
        Order order = sampleOrder();
        OrderDto dto = OrderDto.builder()
            .orderDate(order.getOrderDate())
            .orderDesc("New order")
            .orderFee(50.0)
            .cartDto(CartDto.builder().cartId(4).build())
            .build();
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.save(dto);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("deleteById removes order")
    void deleteByIdRemovesOrder() {
        Order order = sampleOrder();
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        orderService.deleteById(7);

        verify(orderRepository).findById(7);
        verify(orderRepository).delete(any(Order.class));
    }

    private Order sampleOrder() {
        Cart cart = Cart.builder()
            .cartId(4)
            .userId(2)
            .build();

        return Order.builder()
            .orderId(7)
            .orderDate(LocalDateTime.of(2023, 6, 10, 9, 30))
            .orderDesc("Test order")
            .orderFee(30.0)
            .cart(cart)
            .build();
    }
}
