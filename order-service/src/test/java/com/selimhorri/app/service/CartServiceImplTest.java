package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.service.impl.CartServiceImpl;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("findAll enriches carts with user details")
    void findAllEnrichesWithUser() {
        Cart cart = sampleCart();
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        UserDto userDto = UserDto.builder().userId(1).firstName("John").build();
        when(restTemplate.getForObject(anyString(), any(Class.class))).thenReturn(userDto);

        List<CartDto> result = cartService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserDto()).isNotNull();
        verify(restTemplate).getForObject(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("findById returns enriched cart")
    void findByIdReturnsEnriched() {
        Cart cart = sampleCart();
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        UserDto userDto = UserDto.builder().userId(1).firstName("John").build();
        when(restTemplate.getForObject(anyString(), any(Class.class))).thenReturn(userDto);

        CartDto result = cartService.findById(1);

        assertThat(result.getCartId()).isEqualTo(1);
        assertThat(result.getUserDto()).isNotNull();
        verify(cartRepository).findById(1);
    }

    @Test
    @DisplayName("findById throws when cart not found")
    void findByIdThrowsWhenMissing() {
        when(cartRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.findById(99))
            .isInstanceOf(CartNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save creates new cart")
    void saveCreatesCart() {
        Cart cart = sampleCart();
        CartDto dto = CartDto.builder().userId(1).build();
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.save(dto);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("update modifies existing cart")
    void updateModifiesCart() {
        Cart cart = sampleCart();
        CartDto dto = CartDto.builder().cartId(1).userId(1).build();
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.update(dto);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("update with id updates existing cart")
    void updateWithIdUpdatesCart() {
        Cart cart = sampleCart();
        CartDto dto = CartDto.builder()
            .userId(2)
            .build();
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.update(1, dto);

        assertThat(result).isNotNull();
        verify(cartRepository).findById(1);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("deleteById removes cart")
    void deleteByIdRemovesCart() {
        cartService.deleteById(1);

        verify(cartRepository).deleteById(1);
    }

    private Cart sampleCart() {
        return Cart.builder()
            .cartId(1)
            .userId(1)
            .build();
    }
}

