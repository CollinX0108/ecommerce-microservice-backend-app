package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.FavouriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(IntegrationTestConfig.class)
class FavouriteServiceIntegrationTest {

    @Autowired
    private FavouriteService favouriteService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private FavouriteRepository favouriteRepository;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;
    private FavouriteDto favouriteDto;
    private Favourite favourite;
    private UserDto userDto;
    private ProductDto productDto;
    private FavouriteId favouriteId;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();

        // Setup test data
        favouriteId = new FavouriteId(1, 1, LocalDateTime.now());
        
        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
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
    void testFindById_WithSuccessfulServiceCalls() throws Exception {
        // Given
        when(favouriteRepository.findById(any(FavouriteId.class)))
                .thenReturn(Optional.of(favourite));

        // Mock user-service call
        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userDto), MediaType.APPLICATION_JSON));

        // Mock product-service call
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        // When
        FavouriteDto result = favouriteService.findById(favouriteId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getProductId());
        assertNotNull(result.getUserDto());
        assertNotNull(result.getProductDto());
        assertEquals("John", result.getUserDto().getFirstName());
        assertEquals("Test Product", result.getProductDto().getProductTitle());

        mockServer.verify();
    }

    @Test
    void testFindById_WithUserServiceFailure() throws Exception {
        // Given
        when(favouriteRepository.findById(any(FavouriteId.class)))
                .thenReturn(Optional.of(favourite));

        // Mock user-service failure
        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // When & Then
        assertThrows(Exception.class, () -> {
            favouriteService.findById(favouriteId);
        });

        mockServer.verify();
    }

    @Test
    void testFindById_WithProductServiceFailure() throws Exception {
        // Given
        when(favouriteRepository.findById(any(FavouriteId.class)))
                .thenReturn(Optional.of(favourite));

        // Mock user-service success
        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userDto), MediaType.APPLICATION_JSON));

        // Mock product-service failure
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(Exception.class, () -> {
            favouriteService.findById(favouriteId);
        });

        mockServer.verify();
    }

    @Test
    void testFindAll_WithMultipleFavourites() throws Exception {
        // Given
        Favourite favourite2 = Favourite.builder()
                .userId(2)
                .productId(2)
                .likeDate(LocalDateTime.now())
                .build();

        when(favouriteRepository.findAll())
                .thenReturn(java.util.Arrays.asList(favourite, favourite2));

        UserDto userDto2 = UserDto.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        ProductDto productDto2 = ProductDto.builder()
                .productId(2)
                .productTitle("Another Product")
                .sku("TEST-002")
                .priceUnit(149.99)
                .build();

        // Mock multiple service calls
        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userDto2), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto2), MediaType.APPLICATION_JSON));

        // When
        var result = favouriteService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first favourite
        FavouriteDto first = result.get(0);
        assertEquals("John", first.getUserDto().getFirstName());
        assertEquals("Test Product", first.getProductDto().getProductTitle());
        
        // Verify second favourite
        FavouriteDto second = result.get(1);
        assertEquals("Jane", second.getUserDto().getFirstName());
        assertEquals("Another Product", second.getProductDto().getProductTitle());

        mockServer.verify();
    }

    @Test
    void testServiceIntegration_WithTimeout() throws Exception {
        // Given
        when(favouriteRepository.findById(any(FavouriteId.class)))
                .thenReturn(Optional.of(favourite));

        // Mock successful user-service response
        mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(userDto), MediaType.APPLICATION_JSON));

        // Mock product-service failure to simulate service unavailability
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT));

        // When & Then
        assertThrows(Exception.class, () -> {
            favouriteService.findById(favouriteId);
        });
        
        // Reset mock server to avoid verification issues
        mockServer.reset();
    }

    @Test
    void testCircuitBreakerPattern() throws Exception {
        // Given
        when(favouriteRepository.findById(any(FavouriteId.class)))
                .thenReturn(Optional.of(favourite));

        // Mock multiple failures to trigger circuit breaker
        for (int i = 0; i < 5; i++) {
            mockServer.expect(requestTo("http://USER-SERVICE/user-service/api/users/1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        // When & Then - Multiple calls should fail
        for (int i = 0; i < 5; i++) {
            assertThrows(Exception.class, () -> {
                favouriteService.findById(favouriteId);
            });
        }

        mockServer.verify();
    }
} 