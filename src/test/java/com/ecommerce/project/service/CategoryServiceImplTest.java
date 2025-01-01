package com.ecommerce.project.service;

import com.ecommerce.project.exception.ApiException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    private static final Long TEST_CATEGORY_ID = 1L;
    private static final String TEST_CATEGORY_NAME = "Category name";
    private static final Category TEST_CATEGORY = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);

    @Mock
    private CategoryRepository categoryRepositoryMock;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testGetAllCategories_Success() throws ApiException {
        // given
        List<Category> categories = List.of(TEST_CATEGORY);
        when(categoryRepositoryMock.findAll()).thenReturn(categories);

        // when
        List<Category> allCategories = categoryService.getAllCategories();

        // then
        assertThat(allCategories).isSameAs(categories);
        verify(categoryRepositoryMock).findAll();
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testGetAllCategories_NoCategoriesExist() throws ApiException {
        // given
        when(categoryRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.getAllCategories());

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ApiException.class)
                .hasMessage("No category created till now.");
        verify(categoryRepositoryMock).findAll();
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testCreateCategory_Success() {
        // given
        when(categoryRepositoryMock.findByCategoryName(TEST_CATEGORY_NAME)).thenReturn(null);

        // when
        categoryService.createCategory(TEST_CATEGORY);

        // then
        verify(categoryRepositoryMock).findByCategoryName(TEST_CATEGORY_NAME);
        verify(categoryRepositoryMock).save(TEST_CATEGORY);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testCreateCategory_CategoryAlreadyExists() throws ApiException {
        // given
        when(categoryRepositoryMock.findByCategoryName(TEST_CATEGORY_NAME)).thenReturn(TEST_CATEGORY);

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.createCategory(TEST_CATEGORY));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ApiException.class)
                .hasMessage("Category with name Category name already exists!");
        verify(categoryRepositoryMock).findByCategoryName(TEST_CATEGORY_NAME);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testDeleteCategory_Success() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));

        // when
        String result = categoryService.deleteCategory(TEST_CATEGORY_ID);

        // then
        assertEquals("Category with categoryId: 1 deleted successfully!", result);
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verify(categoryRepositoryMock).delete(TEST_CATEGORY);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testDeleteCategory_CategoryNotFound() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.deleteCategory(TEST_CATEGORY_ID));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with categoryId 1");
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testUpdateCategory_Success() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));
        String categoryNameAfterUpdate = "New category name";
        Category categoryToUpdate = new Category(TEST_CATEGORY_ID, categoryNameAfterUpdate);
        when(categoryRepositoryMock.save(categoryToUpdate)).thenReturn(categoryToUpdate);

        // when
        Category result = categoryService.updateCategory(categoryToUpdate, TEST_CATEGORY_ID);

        // then
        assertNotNull(result);
        assertEquals(categoryNameAfterUpdate, result.getCategoryName());
        assertEquals(TEST_CATEGORY_ID, result.getCategoryId());
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verify(categoryRepositoryMock).save(categoryToUpdate);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }

    @Test
    void testUpdateCategory_CategoryNotFound() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.updateCategory(TEST_CATEGORY, TEST_CATEGORY_ID));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with categoryId 1");
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock);
    }
}