package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.exception.ApiException;
import com.pgedlek.ecommerce.exception.ResourceNotFoundException;
import com.pgedlek.ecommerce.model.Category;
import com.pgedlek.ecommerce.payload.CategoryDTO;
import com.pgedlek.ecommerce.payload.CategoryResponse;
import com.pgedlek.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static com.pgedlek.ecommerce.config.AppConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    private static final Integer TEST_PAGE_NUMBER = Integer.valueOf(PAGE_NUMBER);
    private static final Integer TEST_PAGE_SIZE = Integer.valueOf(PAGE_SIZE);
    private static final String TEST_SORT_BY = SORT_CATEGORIES_BY;
    private static final String TEST_SORT_ORDER = SORT_DIR;
    private static final Long TEST_CATEGORY_ID = 1L;
    private static final String TEST_CATEGORY_NAME = "Category name";
    private static final Category TEST_CATEGORY_WITHOUT_ID = new Category(null, TEST_CATEGORY_NAME, List.of());
    private static final Category TEST_CATEGORY = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME, List.of());
    private static final CategoryDTO TEST_CATEGORY_DTO_WITHOUT_ID = new CategoryDTO(null, TEST_CATEGORY_NAME);
    private static final CategoryDTO TEST_CATEGORY_DTO = new CategoryDTO(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);

    @Mock
    private CategoryRepository categoryRepositoryMock;
    @Mock
    private ModelMapper modelMapperMock;

    @InjectMocks
    private CategoryServiceImpl categoryService;


    @Test
    void testGetAllCategories_Success() throws ApiException {
        // given
        List<Category> categories = List.of(TEST_CATEGORY);
        Page<Category> categoryPage = new PageImpl<>(categories);
        when(categoryRepositoryMock.findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))).thenReturn(categoryPage);
        when(modelMapperMock.map(TEST_CATEGORY, CategoryDTO.class)).thenReturn(TEST_CATEGORY_DTO);

        // when
        CategoryResponse allCategories = categoryService.getAllCategories(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER);

        // then
        assertThat(allCategories.getContent()).hasSameElementsAs(List.of(TEST_CATEGORY_DTO));
        verify(categoryRepositoryMock).findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verify(modelMapperMock).map(TEST_CATEGORY, CategoryDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testGetAllCategories_NoCategoriesExist() throws ApiException {
        // given
        when(categoryRepositoryMock.findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))).thenReturn(Page.empty());

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.getAllCategories(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ApiException.class)
                .hasMessage("No category created till now.");
        verify(categoryRepositoryMock).findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testCreateCategory_Success() {
        // given
        when(modelMapperMock.map(TEST_CATEGORY_DTO_WITHOUT_ID, Category.class)).thenReturn(TEST_CATEGORY_WITHOUT_ID);
        when(categoryRepositoryMock.findByCategoryName(TEST_CATEGORY_NAME)).thenReturn(null);
        when(categoryRepositoryMock.save(TEST_CATEGORY_WITHOUT_ID)).thenReturn(TEST_CATEGORY);
        when(modelMapperMock.map(TEST_CATEGORY, CategoryDTO.class)).thenReturn(TEST_CATEGORY_DTO);

        // when
        CategoryDTO categoryDTO = categoryService.createCategory(TEST_CATEGORY_DTO_WITHOUT_ID);

        // then
        assertThat(categoryDTO)
                .isSameAs(TEST_CATEGORY_DTO);
        verify(modelMapperMock).map(TEST_CATEGORY_DTO_WITHOUT_ID, Category.class);
        verify(categoryRepositoryMock).findByCategoryName(TEST_CATEGORY_NAME);
        verify(categoryRepositoryMock).save(TEST_CATEGORY_WITHOUT_ID);
        verify(modelMapperMock).map(TEST_CATEGORY, CategoryDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testCreateCategory_CategoryAlreadyExists() throws ApiException {
        // given
        when(modelMapperMock.map(TEST_CATEGORY_DTO_WITHOUT_ID, Category.class)).thenReturn(TEST_CATEGORY_WITHOUT_ID);
        when(categoryRepositoryMock.findByCategoryName(TEST_CATEGORY_NAME)).thenReturn(TEST_CATEGORY);

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.createCategory(TEST_CATEGORY_DTO_WITHOUT_ID));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ApiException.class)
                .hasMessage("Category with name Category name already exists!");
        verify(modelMapperMock).map(TEST_CATEGORY_DTO_WITHOUT_ID, Category.class);
        verify(categoryRepositoryMock).findByCategoryName(TEST_CATEGORY_NAME);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testDeleteCategory_Success() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));
        when(modelMapperMock.map(TEST_CATEGORY, CategoryDTO.class)).thenReturn(TEST_CATEGORY_DTO);

        // when
        CategoryDTO categoryDTO = categoryService.deleteCategory(TEST_CATEGORY_ID);

        // then
        assertThat(categoryDTO).isSameAs(TEST_CATEGORY_DTO);
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verify(categoryRepositoryMock).delete(TEST_CATEGORY);
        verify(modelMapperMock).map(TEST_CATEGORY, CategoryDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
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
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testUpdateCategory_Success() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));
        String categoryNameAfterUpdate = "New category name";
        Category categoryToUpdate = new Category(TEST_CATEGORY_ID, categoryNameAfterUpdate, List.of());
        CategoryDTO categoryDtoToUpdate = new CategoryDTO(TEST_CATEGORY_ID, categoryNameAfterUpdate);
        when(modelMapperMock.map(categoryDtoToUpdate, Category.class)).thenReturn(categoryToUpdate);
        when(categoryRepositoryMock.save(categoryToUpdate)).thenReturn(categoryToUpdate);
        when(modelMapperMock.map(categoryToUpdate, CategoryDTO.class)).thenReturn(categoryDtoToUpdate);

        // when
        CategoryDTO result = categoryService.updateCategory(categoryDtoToUpdate, TEST_CATEGORY_ID);

        // then
        assertNotNull(result);
        assertEquals(categoryNameAfterUpdate, result.getCategoryName());
        assertEquals(TEST_CATEGORY_ID, result.getCategoryId());
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verify(modelMapperMock).map(categoryDtoToUpdate, Category.class);
        verify(categoryRepositoryMock).save(categoryToUpdate);
        verify(modelMapperMock).map(categoryToUpdate, CategoryDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }

    @Test
    void testUpdateCategory_CategoryNotFound() {
        // given
        when(categoryRepositoryMock.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> categoryService.updateCategory(new CategoryDTO(), TEST_CATEGORY_ID));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with categoryId 1");
        verify(categoryRepositoryMock).findById(TEST_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock, modelMapperMock);
    }
}