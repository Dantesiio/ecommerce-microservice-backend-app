package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
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
import com.selimhorri.app.dto.ProductDto;
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
    @DisplayName("findById returns product when exists")
    void findByIdReturnsProduct() {
        Product product = sampleProduct();
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        ProductDto result = productService.findById(5);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(5);
        verify(productRepository).findById(5);
    }

    @Test
    @DisplayName("findById propagates not found exception")
    void findByIdThrowsWhenMissing() {
        when(productRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(9))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("save creates new product")
    void saveCreatesProduct() {
        Product product = sampleProduct();
        ProductDto dto = ProductDto.builder()
            .productTitle("Laptop")
            .sku("SKU-456")
            .priceUnit(999.99)
            .quantity(5)
            .categoryDto(com.selimhorri.app.dto.CategoryDto.builder().categoryId(3).build())
            .build();
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.save(dto);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(5);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("update updates existing product")
    void updateUpdatesProduct() {
        Product product = sampleProduct();
        ProductDto dto = ProductDto.builder()
            .productId(5)
            .productTitle("Updated Phone")
            .sku("SKU-123")
            .categoryDto(com.selimhorri.app.dto.CategoryDto.builder().categoryId(3).build())
            .build();
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(5);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("update with id updates existing product")
    void updateWithIdUpdatesProduct() {
        Product product = sampleProduct();
        ProductDto dto = ProductDto.builder()
            .productTitle("Updated Phone")
            .build();
        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.update(5, dto);

        assertThat(result).isNotNull();
        verify(productRepository).findById(5);
        verify(productRepository).save(any(Product.class));
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
