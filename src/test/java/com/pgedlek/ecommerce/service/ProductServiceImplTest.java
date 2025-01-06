package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.exception.ApiException;
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
    private static final Product TEST_PRODUCT = new Product(1L, "Name", "default.png", "Description", 1, 1.0, 0.0, 1.0, null);
    private static final ProductDTO TEST_PRODUCT_DTO = new ProductDTO(1L, "Name", "default.png", "Description", 1, 1.0, 0.0, 1.0);

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
}