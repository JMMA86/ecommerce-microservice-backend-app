package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
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

    private Product product;
    private ProductDto productDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1);
        category.setCategoryTitle("Electronics");
        category.setImageUrl("electronics.jpg");

        categoryDto = new CategoryDto();
        categoryDto.setCategoryId(1);
        categoryDto.setCategoryTitle("Electronics");
        categoryDto.setImageUrl("electronics.jpg");

        product = new Product();
        product.setProductId(1);
        product.setProductTitle("Laptop");
        product.setImageUrl("laptop.jpg");
        product.setSku("LAPTOP-001");
        product.setPriceUnit(999.99);
        product.setQuantity(10);
        product.setCategory(category);

        productDto = new ProductDto();
        productDto.setProductId(1);
        productDto.setProductTitle("Laptop");
        productDto.setImageUrl("laptop.jpg");
        productDto.setSku("LAPTOP-001");
        productDto.setPriceUnit(999.99);
        productDto.setQuantity(10);
        productDto.setCategoryDto(categoryDto);
    }

    @Test
    void testFindAll() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        List<ProductDto> result = productService.findAll();
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        ProductDto result = productService.findById(1);
        assertEquals(productDto.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.findById(1));
    }

    @Test
    void testSave() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.save(productDto);
        assertEquals(productDto.getProductId(), result.getProductId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdate() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.update(productDto);
        assertEquals(productDto.getProductId(), result.getProductId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateWithId() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.update(1, productDto);
        assertEquals(productDto.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        productService.deleteById(1);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

}