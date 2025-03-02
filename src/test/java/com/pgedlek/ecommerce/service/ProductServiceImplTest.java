package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.exception.ApiException;
import com.pgedlek.ecommerce.exception.ResourceNotFoundException;
import com.pgedlek.ecommerce.model.Cart;
import com.pgedlek.ecommerce.model.Category;
import com.pgedlek.ecommerce.model.Product;
import com.pgedlek.ecommerce.payload.ProductDTO;
import com.pgedlek.ecommerce.payload.ProductResponse;
import com.pgedlek.ecommerce.repository.CartRepository;
import com.pgedlek.ecommerce.repository.CategoryRepository;
import com.pgedlek.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Mock
    private FileService fileServiceMock;
    @Mock
    private CartRepository cartRepositoryMock;
    @Mock
    private CartService cartServiceMock;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    public void setUp() {
        productService.setImageBaseUrl("http://abc.com/images");
    }

    @Test
    public void testGetAllProducts_Success() {
        // given
        List<Product> productList = List.of(TEST_PRODUCT);
        when(productRepositoryMock.findAll(any(Specification.class),
                eq(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))))
                .thenReturn(new PageImpl<>(productList));
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);


        // when
        ProductResponse allProducts = productService.getAllProducts(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER, "keyword", "category");

        // then
        assertThat(allProducts.getContent()).hasSameElementsAs(List.of(TEST_PRODUCT_DTO));
        verify(productRepositoryMock).findAll(any(Specification.class),
                eq(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending())));
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(categoryRepositoryMock, productRepositoryMock, modelMapperMock);
    }

    @Test
    void testGetAllProducts_returnEmptyListWhenNoProductFound() throws ApiException {
        // given
        when(productRepositoryMock.findAll(any(Specification.class),
                eq(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()))))
                .thenReturn(Page.empty());

        // when
        ProductResponse productResponse = productService.getAllProducts(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER, null, null);

        // then
        assertThat(productResponse.getContent()).isEmpty();
        verify(productRepositoryMock).findAll(any(Specification.class),
                eq(PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending())));
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
    void testSearchProductByKeyword_Success() {
        // given
        List<Product> products = List.of(TEST_PRODUCT);
        when(productRepositoryMock.findByProductNameLikeIgnoreCase("%Laptop%", PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending())))
                .thenReturn(new PageImpl<>(products));
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductResponse response = productService.searchProductByKeyword("Laptop", TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER);

        // then
        assertThat(response.getContent()).hasSameElementsAs(List.of(TEST_PRODUCT_DTO));
        verify(productRepositoryMock).findByProductNameLikeIgnoreCase("%Laptop%", PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending()));
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(productRepositoryMock, modelMapperMock);
    }

    @Test
    void testSearchProductByKeyword_NoProductsFound() {
        // given
        when(productRepositoryMock.findByProductNameLikeIgnoreCase("%Laptop%", PageRequest.of(TEST_PAGE_NUMBER, TEST_PAGE_SIZE, Sort.by(TEST_SORT_BY).ascending())))
                .thenReturn(new PageImpl<>(List.of()));

        // when
        Throwable caughtException = catchThrowable(() -> productService.searchProductByKeyword("Laptop", TEST_PAGE_NUMBER, TEST_PAGE_SIZE, TEST_SORT_BY, TEST_SORT_ORDER));

        // then
        assertThat(caughtException).isExactlyInstanceOf(ApiException.class)
                .hasMessage("No products found matching keyword Laptop");
        verifyNoMoreInteractions(productRepositoryMock, modelMapperMock);
    }

    @Test
    void testUpdateProduct_Success() {
        // given
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.of(TEST_PRODUCT));
        Product updatedProduct = new Product(PRODUCT_ID, "Updated Name", "updated.png", "Updated Description", 5, 50.0, 5.0, 45.0);
        ProductDTO updatedProductDTO = new ProductDTO(PRODUCT_ID, "Updated Name", "updated.png", "Updated Description", 5, 50.0, 5.0, 45.0);
        when(modelMapperMock.map(updatedProductDTO, Product.class)).thenReturn(updatedProduct);
        when(productRepositoryMock.save(TEST_PRODUCT)).thenReturn(updatedProduct);
        when(modelMapperMock.map(updatedProduct, ProductDTO.class)).thenReturn(updatedProductDTO);

        // when
        ProductDTO result = productService.updateProduct(PRODUCT_ID, updatedProductDTO);

        // then
        assertThat(result).isSameAs(updatedProductDTO);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verify(modelMapperMock).map(updatedProductDTO, Product.class);
        verify(productRepositoryMock).save(TEST_PRODUCT);
        verify(modelMapperMock).map(updatedProduct, ProductDTO.class);
        verifyNoMoreInteractions(productRepositoryMock, modelMapperMock);
    }

    @Test
    void testUpdateProduct_ResourceNotFound() {
        // given
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        ProductDTO productDTO = new ProductDTO(PRODUCT_ID, "Updated Product", "updated.png", "Updated Description", 5, 50.0, 5.0, 45.0);

        // when
        Throwable caughtException = catchThrowable(() -> productService.updateProduct(PRODUCT_ID, productDTO));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with productId " + PRODUCT_ID);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verifyNoMoreInteractions(productRepositoryMock, modelMapperMock);
    }

    @Test
    void testUpdateProductImage_Success() throws Exception {
        // given
        MultipartFile image = mock(MultipartFile.class);
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.of(TEST_PRODUCT));

        String uploadedImageName = "updated_image.png";
        when(fileServiceMock.uploadImage(null, image)).thenReturn(uploadedImageName);
        Product updatedProduct = new Product(PRODUCT_ID, "Name", uploadedImageName, "Description", 1, 1.0, 0.0, 1.0);
        when(productRepositoryMock.save(TEST_PRODUCT)).thenReturn(updatedProduct);
        when(modelMapperMock.map(updatedProduct, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductDTO result = productService.updateProductImage(PRODUCT_ID, image);

        // then
        assertThat(result).isSameAs(TEST_PRODUCT_DTO);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verify(fileServiceMock).uploadImage(null, image);
        verify(productRepositoryMock).save(TEST_PRODUCT);
        verify(modelMapperMock).map(updatedProduct, ProductDTO.class);
        verifyNoMoreInteractions(productRepositoryMock, fileServiceMock, modelMapperMock);
    }

    @Test
    void testUpdateProductImage_ProductNotFound() {
        // given
        MultipartFile image = mock(MultipartFile.class);
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> productService.updateProductImage(PRODUCT_ID, image));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with productId " + PRODUCT_ID);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verifyNoMoreInteractions(productRepositoryMock, fileServiceMock, modelMapperMock);
    }

    @Test
    void testUpdateProductImage_FileUploadError() throws Exception {
        // given
        MultipartFile image = mock(MultipartFile.class);
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.of(TEST_PRODUCT));
        when(fileServiceMock.uploadImage(null, image)).thenThrow(new IOException("File upload failed"));

        // when
        Throwable caughtException = catchThrowable(() -> productService.updateProductImage(PRODUCT_ID, image));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("File upload failed");
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verify(fileServiceMock).uploadImage(null, image);
        verifyNoMoreInteractions(productRepositoryMock, fileServiceMock, modelMapperMock);
    }

    @Test
    void testDeleteProduct_Success() {
        // given
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.of(TEST_PRODUCT));
        when(cartRepositoryMock.findCartsByProductId(PRODUCT_ID)).thenReturn(List.of());
        when(modelMapperMock.map(TEST_PRODUCT, ProductDTO.class)).thenReturn(TEST_PRODUCT_DTO);

        // when
        ProductDTO result = productService.deleteProduct(PRODUCT_ID);

        // then
        assertThat(result).isSameAs(TEST_PRODUCT_DTO);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verify(cartRepositoryMock).findCartsByProductId(PRODUCT_ID);
        verify(productRepositoryMock).delete(TEST_PRODUCT);
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(productRepositoryMock, cartRepositoryMock, modelMapperMock);
    }

    @Test
    void testDeleteProduct_ProductNotFound() {
        // given
        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when
        Throwable caughtException = catchThrowable(() -> productService.deleteProduct(PRODUCT_ID));

        // then
        assertThat(caughtException)
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with productId " + PRODUCT_ID);
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verifyNoMoreInteractions(productRepositoryMock, cartRepositoryMock, modelMapperMock);
    }

    @Test
    void testDeleteProduct_ProductInCart() {
        // given
        Cart cart = new Cart();
        cart.setCartId(1L);
        List<Cart> carts = List.of(cart);

        when(productRepositoryMock.findById(PRODUCT_ID)).thenReturn(Optional.of(TEST_PRODUCT));
        when(cartRepositoryMock.findCartsByProductId(PRODUCT_ID)).thenReturn(carts);

        // when
        productService.deleteProduct(PRODUCT_ID);

        // then
        verify(productRepositoryMock).findById(PRODUCT_ID);
        verify(cartRepositoryMock).findCartsByProductId(PRODUCT_ID);
        verify(cartServiceMock).deleteProductFromCart(cart.getCartId(), PRODUCT_ID);
        verify(productRepositoryMock).delete(TEST_PRODUCT);
        verify(modelMapperMock).map(TEST_PRODUCT, ProductDTO.class);
        verifyNoMoreInteractions(productRepositoryMock, cartRepositoryMock, cartServiceMock, modelMapperMock);
    }
}