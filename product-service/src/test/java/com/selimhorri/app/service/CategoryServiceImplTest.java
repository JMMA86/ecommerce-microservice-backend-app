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
    }

    @Test
    void testFindAll() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));
        List<CategoryDto> result = categoryService.findAll();
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        CategoryDto result = categoryService.findById(1);
        assertEquals(categoryDto.getCategoryId(), result.getCategoryId());
        verify(categoryRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(1));
    }

    @Test
    void testSave() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDto result = categoryService.save(categoryDto);
        assertEquals(categoryDto.getCategoryId(), result.getCategoryId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdate() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDto result = categoryService.update(categoryDto);
        assertEquals(categoryDto.getCategoryId(), result.getCategoryId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdateWithId() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDto result = categoryService.update(1, categoryDto);
        assertEquals(categoryDto.getCategoryId(), result.getCategoryId());
        verify(categoryRepository, times(1)).findById(1);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testDeleteById() {
        categoryService.deleteById(1);
        verify(categoryRepository, times(1)).deleteById(1);
    }

}