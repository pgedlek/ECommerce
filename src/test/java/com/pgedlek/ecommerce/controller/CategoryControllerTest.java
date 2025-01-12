package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.payload.CategoryDTO;
import com.pgedlek.ecommerce.payload.CategoryResponse;
import com.pgedlek.ecommerce.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {
    @Mock
    private CategoryService categoryServiceMock;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void testGetAllCategories_Success() {
        // given
        CategoryResponse categoryResponse = new CategoryResponse(List.of(new CategoryDTO(1L, "Test Category")), 1, 10, 1L, 1, true);
        when(categoryServiceMock.getAllCategories(0, 10, "name", "asc")).thenReturn(categoryResponse);

        // when
        ResponseEntity<CategoryResponse> response = categoryController.getAllCategories(0, 10, "name", "asc");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(categoryResponse);
        verify(categoryServiceMock).getAllCategories(0, 10, "name", "asc");
    }

    @Test
    void testCreateCategory_Success() {
        // given
        CategoryDTO categoryDTO = new CategoryDTO(null, "New Category");
        CategoryDTO savedCategoryDTO = new CategoryDTO(1L, "New Category");
        when(categoryServiceMock.createCategory(categoryDTO)).thenReturn(savedCategoryDTO);

        // when
        ResponseEntity<CategoryDTO> response = categoryController.createCategory(categoryDTO);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(savedCategoryDTO);
        verify(categoryServiceMock).createCategory(categoryDTO);
    }

    @Test
    void testDeleteCategory_Success() {
        // given
        Long categoryId = 1L;
        CategoryDTO deletedCategoryDTO = new CategoryDTO(categoryId, "Deleted Category");
        when(categoryServiceMock.deleteCategory(categoryId)).thenReturn(deletedCategoryDTO);

        // when
        ResponseEntity<CategoryDTO> response = categoryController.deleteCategory(categoryId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(deletedCategoryDTO);
        verify(categoryServiceMock).deleteCategory(categoryId);
    }

    @Test
    void testUpdateCategory_Success() {
        // given
        Long categoryId = 1L;
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Updated Category");
        CategoryDTO updatedCategoryDTO = new CategoryDTO(categoryId, "Updated Category");
        when(categoryServiceMock.updateCategory(newCategoryDTO, categoryId)).thenReturn(updatedCategoryDTO);

        // when
        ResponseEntity<CategoryDTO> response = categoryController.updateCategory(newCategoryDTO, categoryId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(updatedCategoryDTO);
        verify(categoryServiceMock).updateCategory(newCategoryDTO, categoryId);
    }
}