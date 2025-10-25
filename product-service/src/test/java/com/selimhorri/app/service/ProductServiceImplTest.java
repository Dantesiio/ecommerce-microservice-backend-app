package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("findAll returns unmodifiable list of mapped products")
    @SuppressWarnings("unchecked")
    void findAllReturnsUnmodifiableList() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct()));

    List<?> result = productService.findAll();

    assertThat(result).hasSize(1);
        List<Object> mutableView = (List<Object>) (List<?>) result;
        Throwable thrown = catchThrowable(() -> mutableView.add(new Object()));
    assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("deleteById maps DTO and deletes entity")
    void deleteByIdRemovesEntity() {
        Product entity = sampleProduct();
        when(productRepository.findById(5)).thenReturn(Optional.of(entity));

        productService.deleteById(5);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).delete(captor.capture());
        Product deleted = captor.getValue();
        assertThat(deleted.getProductId()).isEqualTo(5);
        assertThat(deleted.getSku()).isEqualTo("SKU-123");
    }

    @Test
    @DisplayName("findById propagates not found exception")
    void findByIdThrowsWhenMissing() {
        when(productRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(9))
            .isInstanceOf(ProductNotFoundException.class);
    }

    private Product sampleProduct() {
        Category category = Category.builder()
            .categoryId(3)
            .categoryTitle("Electronics")
            .imageUrl("/img/cat.png")
            .build();

        return Product.builder()
            .productId(5)
            .productTitle("Phone")
            .imageUrl("/img/phone.png")
            .sku("SKU-123")
            .priceUnit(199.99)
            .quantity(10)
            .category(category)
            .build();
    }
}
