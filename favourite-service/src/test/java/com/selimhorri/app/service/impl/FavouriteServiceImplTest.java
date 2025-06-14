package com.selimhorri.app.service.impl;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.helper.FavouriteMappingHelper;
import com.selimhorri.app.repository.FavouriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private FavouriteDto favouriteDto;
    private Favourite favourite;
    private FavouriteId favouriteId;
    private UserDto userDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        favouriteId = new FavouriteId(1, 1, LocalDateTime.now());

        userDto = UserDto.builder()
                .userId(1)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("test-image.jpg")
                .sku("TEST-001")
                .priceUnit(99.99)
                .quantity(10)
                .build();

        favouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .userDto(userDto)
                .productDto(productDto)
                .build();

        favourite = Favourite.builder()
                .userId(1)
                .productId(1)
                .likeDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testFindAll_ShouldReturnFavouriteListWithUserAndProductData() {
        // Given
        List<Favourite> favourites = Arrays.asList(favourite);
        when(favouriteRepository.findAll()).thenReturn(favourites);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + favouriteDto.getUserId(),
                UserDto.class
        )).thenReturn(userDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + favouriteDto.getProductId(),
                ProductDto.class
        )).thenReturn(productDto);

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When
            List<FavouriteDto> result = favouriteService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(favouriteDto.getUserId(), result.get(0).getUserId());
            assertEquals(favouriteDto.getProductId(), result.get(0).getProductId());
            assertNotNull(result.get(0).getUserDto());
            assertNotNull(result.get(0).getProductDto());
            verify(favouriteRepository).findAll();
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }

    @Test
    void testFindById_ShouldReturnFavouriteWithUserAndProductData_WhenFavouriteExists() {
        // Given
        when(favouriteRepository.findById(favouriteId)).thenReturn(Optional.of(favourite));
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + favouriteDto.getUserId(),
                UserDto.class
        )).thenReturn(userDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + favouriteDto.getProductId(),
                ProductDto.class
        )).thenReturn(productDto);

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When
            FavouriteDto result = favouriteService.findById(favouriteId);

            // Then
            assertNotNull(result);
            assertEquals(favouriteDto.getUserId(), result.getUserId());
            assertEquals(favouriteDto.getProductId(), result.getProductId());
            assertNotNull(result.getUserDto());
            assertNotNull(result.getProductDto());
            verify(favouriteRepository).findById(favouriteId);
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }

    @Test
    void testFindById_ShouldThrowException_WhenFavouriteNotExists() {
        // Given
        when(favouriteRepository.findById(favouriteId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(FavouriteNotFoundException.class, () -> favouriteService.findById(favouriteId));
        verify(favouriteRepository).findById(favouriteId);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testSave_ShouldReturnSavedFavourite() {
        // Given
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(FavouriteDto.class))).thenReturn(favourite);
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When
            FavouriteDto result = favouriteService.save(favouriteDto);

            // Then
            assertNotNull(result);
            assertEquals(favouriteDto.getUserId(), result.getUserId());
            assertEquals(favouriteDto.getProductId(), result.getProductId());
            verify(favouriteRepository).save(any(Favourite.class));
        }
    }

    @Test
    void testUpdate_ShouldReturnUpdatedFavourite() {
        // Given
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(FavouriteDto.class))).thenReturn(favourite);
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When
            FavouriteDto result = favouriteService.update(favouriteDto);

            // Then
            assertNotNull(result);
            assertEquals(favouriteDto.getUserId(), result.getUserId());
            assertEquals(favouriteDto.getProductId(), result.getProductId());
            verify(favouriteRepository).save(any(Favourite.class));
        }
    }

    @Test
    void testDeleteById_ShouldDeleteFavourite() {
        // Given
        // No setup needed for deleteById

        // When
        favouriteService.deleteById(favouriteId);

        // Then
        verify(favouriteRepository).deleteById(favouriteId);
    }

    @Test
    void testFindAll_ShouldHandleEmptyList() {
        // Given
        when(favouriteRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<FavouriteDto> result = favouriteService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favouriteRepository).findAll();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testFindById_ShouldHandleRestTemplateException() {
        // Given
        when(favouriteRepository.findById(favouriteId)).thenReturn(Optional.of(favourite));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenThrow(new RuntimeException("User service unavailable"));

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When & Then
            assertThrows(RuntimeException.class, () -> favouriteService.findById(favouriteId));
            verify(favouriteRepository).findById(favouriteId);
            verify(restTemplate).getForObject(anyString(), eq(UserDto.class));
        }
    }

    @Test
    void testFindAll_ShouldHandleNullUserServiceResponse() {
        // Given
        List<Favourite> favourites = Arrays.asList(favourite);
        when(favouriteRepository.findAll()).thenReturn(favourites);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + favouriteDto.getUserId(),
                UserDto.class
        )).thenReturn(null);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + favouriteDto.getProductId(),
                ProductDto.class
        )).thenReturn(productDto);

        try (MockedStatic<FavouriteMappingHelper> mockedHelper = mockStatic(FavouriteMappingHelper.class)) {
            mockedHelper.when(() -> FavouriteMappingHelper.map(any(Favourite.class))).thenReturn(favouriteDto);

            // When
            List<FavouriteDto> result = favouriteService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(favouriteRepository).findAll();
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }
} 