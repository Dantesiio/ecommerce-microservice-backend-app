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
import java.time.LocalDateTime;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    @Test
    @DisplayName("findAll enriches favourites with user and product")
    void findAllEnrichesRelationships() {
        Favourite favourite = sampleFavourite();
        when(favouriteRepository.findAll()).thenReturn(List.of(favourite));
        when(restTemplate.getForObject(anyString(), any(Class.class)))
            .thenReturn(UserDto.builder().userId(1).build())
            .thenReturn(ProductDto.builder().productId(1).build());

        List<FavouriteDto> result = favouriteService.findAll();

        assertThat(result).hasSize(1);
        verify(restTemplate, org.mockito.Mockito.atLeastOnce()).getForObject(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("findById returns enriched favourite")
    void findByIdReturnsEnriched() {
        Favourite favourite = sampleFavourite();
        LocalDateTime likeDate = LocalDateTime.now();
        FavouriteId id = new FavouriteId(1, 1, likeDate);
        when(favouriteRepository.findById(id)).thenReturn(Optional.of(favourite));
        when(restTemplate.getForObject(anyString(), any(Class.class)))
            .thenReturn(UserDto.builder().userId(1).build())
            .thenReturn(ProductDto.builder().productId(1).build());

        FavouriteDto result = favouriteService.findById(id);

        assertThat(result).isNotNull();
        verify(favouriteRepository).findById(id);
    }

    @Test
    @DisplayName("findById throws when favourite not found")
    void findByIdThrowsWhenMissing() {
        LocalDateTime likeDate = LocalDateTime.now();
        FavouriteId id = new FavouriteId(1, 1, likeDate);
        when(favouriteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteService.findById(id))
            .isInstanceOf(FavouriteNotFoundException.class);
    }

    @Test
    @DisplayName("save creates new favourite")
    void saveCreatesFavourite() {
        Favourite favourite = sampleFavourite();
        FavouriteDto dto = FavouriteDto.builder().userId(1).productId(1).build();
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);

        FavouriteDto result = favouriteService.save(dto);

        assertThat(result).isNotNull();
        verify(favouriteRepository).save(any(Favourite.class));
    }

    @Test
    @DisplayName("update updates existing favourite")
    void updateUpdatesFavourite() {
        Favourite favourite = sampleFavourite();
        FavouriteDto dto = FavouriteDto.builder()
            .userId(1)
            .productId(1)
            .build();
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);

        FavouriteDto result = favouriteService.update(dto);

        assertThat(result).isNotNull();
        verify(favouriteRepository).save(any(Favourite.class));
    }

    @Test
    @DisplayName("deleteById removes favourite")
    void deleteByIdRemovesFavourite() {
        LocalDateTime likeDate = LocalDateTime.now();
        FavouriteId id = new FavouriteId(1, 1, likeDate);
        favouriteService.deleteById(id);

        verify(favouriteRepository).deleteById(id);
    }

    private Favourite sampleFavourite() {
        LocalDateTime likeDate = LocalDateTime.now();
        return Favourite.builder()
            .userId(1)
            .productId(1)
            .likeDate(likeDate)
            .build();
    }
}

