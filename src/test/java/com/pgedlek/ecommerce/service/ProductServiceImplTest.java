package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.exception.ApiException;
import com.pgedlek.ecommerce.exception.ResourceNotFoundException;
import com.pgedlek.ecommerce.model.Category;
import com.pgedlek.ecommerce.model.Product;
import com.pgedlek.ecommerce.payload.ProductDTO;
import com.pgedlek.ecommerce.payload.ProductResponse;
import com.pgedlek.ecommerce.repository.CategoryRepository;
import com.pgedlek.ecommerce.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    private static final Integer TEST_PAGE_NUMBER = Integer.valueOf(PAGE_NUMBER);
    private static final Integer TEST_PAGE_SIZE = Integer.valueOf(PAGE_SIZE);
    private static final String TEST_SORT_BY = SORT_PRODUCTS_BY;
    private static final String TEST_SORT_ORDER = SORT_DIR;
    private static final Long PRODUCT_ID = 1L;
    private static final Product TEST_PRODUCT = new Product(PRODUCT_ID, "Name", "default.png", "Description", 1, 1.0, 0.0, 1.0);
    private static final ProductDTO TEST_PRODUCT_DTO = new ProductDTO(PRODUCT_ID, "Name", "default.png", "Description", 1, 1.0, 0.0, 1.0);
    private static final Long CATEGORY_ID = 10L;
    private static final Category TEST_CATEGORY = new Category(CATEGORY_ID, "Category name", List.of(TEST_PRODUCT));

    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private CategoryRepository categoryRepositoryMock;
    @Mock
    private ModelMapper modelMapperMock;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void testGetAllProducts_Success() {
        // given
        List<Product> productList = List.of(TEST_PRODUCT);
        when(productRepositoryMock.findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending())))
                .thenReturn(new PageImpl<>(productList));
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductResponse allProducts = productService.getAllProducts(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER);

        // then
        assertThat(allProducts.getContent()).hasSameElementsAs(List.of(TEST_PRODUCT_DTO));
        verify(productRepositoryMock).findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    void testGetAllProducts_NoProductsExist() throws ApiException {
        // given
        when(productRepositoryMock.findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))).thenReturn(Page.empty());

        // when
        Throwable caughtException = catchThrowable(() -> productService.getAllProducts(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ApiException.class)
                .hasMessage("No products created till now.");
        verify(productRepositoryMock).findAll(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    void testSearchByCategory_Success() {
        // given
        List<Product> productList = List.of(TEST_PRODUCT);
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));
        when(productRepositoryMock.findByCategoryOrderByPriceAsc(TEST_CATEGORY, PageRequest.of(TEST_PAGE_NUMBER,
                TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))).thenReturn(new PageImpl<>(productList));
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductResponse allProducts = productService.searchByCategory(CATEGORY_ID, TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER);

        // then
        assertThat(allProducts.getContent()).hasSameElementsAs(List.of(TEST_PRODUCT_DTO));
        verify(categoryRepositoryMock).findById(CATEGORY_ID);
        verify(productRepositoryMock).findByCategoryOrderByPriceAsc(TEST_CATEGORY, PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    void testSearchByCategory_ResourceNotFound() {
        // given
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> productService.searchByCategory(CATEGORY_ID, TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER));

        // then
        assertThat(caughtException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with categoryId " + CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    void testSearchByCategory_NoProductsFound() {
        // given
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));
        when(productRepositoryMock.findByCategoryOrderByPriceAsc(TEST_CATEGORY, PageRequest.of(TEST_PAGE_NUMBER,
                TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))).thenReturn(new PageImpl<>(List.of()));

        // when
        Throwable caughtException = catchThrowable(() -> productService.searchByCategory(CATEGORY_ID, TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER));

        // then
        assertThat(caughtException).isExactlyInstanceOf(ApiException.class)
                .hasMessage("No products found with categoryId " + CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    public void testAddProduct_Success() {
        // given
        Product product = new Product();
        ProductDTO productDTO = new ProductDTO();
        Category category = new Category(111L, "Other category name", List.of());
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(modelMapperMock.map(productDTO, Product.class)).thenReturn(product);
        when(productRepositoryMock.save(product)).thenReturn(TEST_PRODUCT);
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductDTO result = productService.addProduct(CATEGORY_ID, productDTO);

        // then
        assertThat(result).isSameAs(TEST_PRODUCT_DTO);
        verify(categoryRepositoryMock).findById(CATEGORY_ID);
        verify(modelMapperMock).map(productDTO, Product.class);
        verify(productRepositoryMock).save(product);
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    public void testAddProduct_ResourceNotFound() {
        // given
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> productService.addProduct(CATEGORY_ID, TEST_PRODUCT_DTO));

        // then
        assertThat(caughtException).isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with categoryId " + CATEGORY_ID);
        verify(categoryRepositoryMock).findById(CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    public void testAddProduct_ProductAlreadyExists() {
        // given
        when(categoryRepositoryMock.findById(CATEGORY_ID)).thenReturn(Optional.of(TEST_CATEGORY));

        // when
        Throwable caughtException = catchThrowable(() -> productService.addProduct(CATEGORY_ID, TEST_PRODUCT_DTO));

        // then
        assertThat(caughtException).isExactlyInstanceOf(ApiException.class)
                .hasMessage("Product with name " +  TEST_PRODUCT_DTO.getProductName() + " already exists!");
        verify(categoryRepositoryMock).findById(CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }
}