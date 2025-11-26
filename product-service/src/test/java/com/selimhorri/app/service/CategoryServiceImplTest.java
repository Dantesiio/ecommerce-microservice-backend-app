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

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("findAll returns list of categories")
    void findAllReturnsList() {
        Category category = sampleCategory();
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDto> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("findById returns DTO when category exists")
    void findByIdReturnsDto() {
        Category category = sampleCategory();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.findById(1);

        assertThat(result.getCategoryId()).isEqualTo(1);
        assertThat(result.getCategoryTitle()).isEqualTo("Electronics");
        verify(categoryRepository).findById(1);
    }

    @Test
    @DisplayName("findById throws when category not found")
    void findByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99))
            .isInstanceOf(CategoryNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save creates new category")
    void saveCreatesCategory() {
        Category category = sampleCategory();
        CategoryDto dto = CategoryDto.builder()
            .categoryTitle("New Category")
            .imageUrl("/img/new.png")
            .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.save(dto);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("update updates existing category")
    void updateUpdatesCategory() {
        Category category = sampleCategory();
        CategoryDto dto = CategoryDto.builder()
            .categoryId(1)
            .categoryTitle("Updated Electronics")
            .imageUrl("/img/updated.png")
            .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.update(dto);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("update with id updates existing category")
    void updateWithIdUpdatesCategory() {
        Category category = sampleCategory();
        CategoryDto dto = CategoryDto.builder()
            .categoryTitle("Updated Electronics")
            .build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.update(1, dto);

        assertThat(result).isNotNull();
        verify(categoryRepository).findById(1);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("deleteById removes category")
    void deleteByIdRemovesCategory() {
        categoryService.deleteById(1);

        verify(categoryRepository).deleteById(1);
    }

    private Category sampleCategory() {
        return Category.builder()
            .categoryId(1)
            .categoryTitle("Electronics")
            .imageUrl("/img/electronics.png")
            .build();
    }
}

