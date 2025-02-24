package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.payload.ProductDTO;
import com.pgedlek.ecommerce.payload.ProductResponse;
import com.pgedlek.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productServiceMock;

    @InjectMocks
    private ProductController productController;

    @Test
    void testAddProduct_Success() {
        // given
        Long categoryId = 1L;
        ProductDTO productDTO = new ProductDTO(1L, "Product", "image.png", "Description", 10, 100.0, 0.1, 99.9);
        when(productServiceMock.addProduct(categoryId, productDTO)).thenReturn(productDTO);

        // when
        ResponseEntity<ProductDTO> response = productController.addProduct(productDTO, categoryId);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productDTO);
        verify(productServiceMock).addProduct(categoryId, productDTO);
    }

    @Test
    void testGetAllProducts_Success() {
        // given
        ProductResponse productResponse = new ProductResponse(List.of(), 1, 10, 1L, 1, true);
        when(productServiceMock.getAllProducts(0, 10, "name", "asc", "keyword", "category")).thenReturn(productResponse);

        // when
        ResponseEntity<ProductResponse> response = productController.getAllProducts("keyword", "category", 0, 10, "name", "asc");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productResponse);
        verify(productServiceMock).getAllProducts(0, 10, "name", "asc", "keyword", "category");
    }

    @Test
    void testGetProductsByCategory_Success() {
        // given
        Long categoryId = 1L;
        ProductResponse productResponse = new ProductResponse(List.of(), 1, 10, 1L, 1, true);
        when(productServiceMock.searchByCategory(categoryId, 0, 10, "name", "asc")).thenReturn(productResponse);

        // when
        ResponseEntity<ProductResponse> response = productController.getProductsByCategory(categoryId, 0, 10, "name", "asc");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productResponse);
        verify(productServiceMock).searchByCategory(categoryId, 0, 10, "name", "asc");
    }

    @Test
    void testGetProductsByKeyword_Success() {
        // given
        String keyword = "Laptop";
        ProductResponse productResponse = new ProductResponse(List.of(), 1, 10, 1L, 1, true);
        when(productServiceMock.searchProductByKeyword(keyword, 0, 10, "name", "asc")).thenReturn(productResponse);

        // when
        ResponseEntity<ProductResponse> response = productController.getProductsByKeyword(keyword, 0, 10, "name", "asc");

        // then
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productResponse);
        verify(productServiceMock).searchProductByKeyword(keyword, 0, 10, "name", "asc");
    }

    @Test
    void testUpdateProduct_Success() {
        // given
        Long productId = 1L;
        ProductDTO productDTO = new ProductDTO(productId, "Updated Product", "updated.png", "Updated Description", 15, 200.0, 0.2, 199.8);
        when(productServiceMock.updateProduct(productId, productDTO)).thenReturn(productDTO);

        // when
        ResponseEntity<ProductDTO> response = productController.updateProduct(productDTO, productId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productDTO);
        verify(productServiceMock).updateProduct(productId, productDTO);
    }

    @Test
    void testDeleteProduct_Success() {
        // given
        Long productId = 1L;
        ProductDTO productDTO = new ProductDTO(productId, "Deleted Product", "image.png", "Description", 10, 100.0, 0.1, 99.9);
        when(productServiceMock.deleteProduct(productId)).thenReturn(productDTO);

        // when
        ResponseEntity<ProductDTO> response = productController.deleteProduct(productId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productDTO);
        verify(productServiceMock).deleteProduct(productId);
    }

    @Test
    void testUpdateProductImage_Success() throws IOException {
        // given
        Long productId = 1L;
        MultipartFile image = mock(MultipartFile.class);
        ProductDTO productDTO = new ProductDTO(productId, "Product with updated image", "new_image.png", "Description", 10, 100.0, 0.1, 99.9);
        when(productServiceMock.updateProductImage(productId, image)).thenReturn(productDTO);

        // when
        ResponseEntity<ProductDTO> response = productController.updateProductImage(productId, image);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(productDTO);
        verify(productServiceMock).updateProductImage(productId, image);
    }
}