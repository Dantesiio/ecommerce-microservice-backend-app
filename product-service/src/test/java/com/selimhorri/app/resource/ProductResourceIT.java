package com.selimhorri.app.resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:product_resource_it;DB_CLOSE_DELAY=-1",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.zipkin.enabled=false",
        "spring.cloud.config.enabled=false",
        "SPRING_CONFIG_IMPORT=optional:file:./"
    })
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ProductResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        electronics = categoryRepository.save(Category.builder()
            .categoryTitle("Electronics")
            .imageUrl("https://cdn.example.com/electronics.png")
            .build());
    }

    @Test
    @DisplayName("GET /product-service/api/products/{id} devuelve el producto con la categoría asociada")
    void findByIdReturnsProductWithCategory() throws Exception {
        Product saved = productRepository.save(Product.builder()
            .productTitle("Smartphone")
            .sku("SKU-IT-001")
            .priceUnit(499.99)
            .quantity(12)
            .imageUrl("https://cdn.example.com/sku-it-001.png")
            .category(electronics)
            .build());

        mockMvc.perform(get("/product-service/api/products/{productId}", saved.getProductId()).contextPath("/product-service"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(saved.getProductId()))
            .andExpect(jsonPath("$.productTitle").value("Smartphone"))
            .andExpect(jsonPath("$.category.categoryId").value(electronics.getCategoryId()))
            .andExpect(jsonPath("$.category.categoryTitle").value("Electronics"));
    }

    @Test
    @DisplayName("POST /product-service/api/products persiste un producto cuando la categoría existe")
    void saveProductPersistsEntity() throws Exception {
        ProductDto payload = ProductDto.builder()
            .productTitle("Gaming Laptop")
            .sku("SKU-IT-002")
            .priceUnit(1299.0)
            .quantity(3)
            .categoryDto(CategoryDto.builder()
                .categoryId(electronics.getCategoryId())
                .categoryTitle(electronics.getCategoryTitle())
                .imageUrl(electronics.getImageUrl())
                .build())
            .build();

    mockMvc.perform(post("/product-service/api/products").contextPath("/product-service")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").isNumber())
            .andExpect(jsonPath("$.productTitle").value("Gaming Laptop"))
            .andExpect(jsonPath("$.category.categoryId").value(electronics.getCategoryId()));

        org.assertj.core.api.Assertions.assertThat(productRepository.findAll())
            .anyMatch(product -> "SKU-IT-002".equals(product.getSku()));
    }
}
