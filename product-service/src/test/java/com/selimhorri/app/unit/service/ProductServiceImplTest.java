package com.selimhorri.app.unit.service;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import com.selimhorri.app.unit.util.ProductUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDto product;

    @BeforeEach
    void setUp() {
        product = ProductUtil.getSampleProductDto();
    }

    @Test
    void testFindById_ShouldReturnProductDto() {
        when(productRepository.findById(1)).thenReturn(Optional.of(ProductUtil.getSampleProduct()));

        ProductDto result = productService.findById(product.getProductId());

        assertNotNull(result);
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getProductTitle(), result.getProductTitle());
        assertEquals(product.getImageUrl(), result.getImageUrl());
    }

    @Test
    void testFindById_ShouldThrowException_WhenProductNotFound() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.findById(999));
        verify(productRepository).findById(999);
    }

    @Test
    void testSave_ShouldReturnSavedProductDto() {
        when(productRepository.save(any())).thenReturn(ProductUtil.getSampleProduct());

        ProductDto result = productService.save(product);

        assertNotNull(result);
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getProductTitle(), result.getProductTitle());
        verify(productRepository).save(any());
    }

    @Test
    void testUpdate_ShouldReturnUpdatedProductDto() {
        when(productRepository.save(any())).thenReturn(ProductUtil.getSampleProduct());

        ProductDto result = productService.update(product);

        assertNotNull(result);
        assertEquals(product.getProductId(), result.getProductId());
        assertEquals(product.getProductTitle(), result.getProductTitle());
        verify(productRepository).save(any());
    }

    @Test
    void testDeleteById_ShouldDeleteProduct() {
        when(productRepository.findById(product.getProductId())).thenReturn(Optional.of(ProductUtil.getSampleProduct()));

        productService.deleteById(product.getProductId());

        verify(productRepository).findById(product.getProductId());
        verify(productRepository).delete(any());
    }

    @Test
    void testFindAll_ShouldReturnProductList() {
        when(productRepository.findAll()).thenReturn(ProductUtil.getSampleProducts());

        var result = productService.findAll();

        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(productRepository).findAll();
    }
} 