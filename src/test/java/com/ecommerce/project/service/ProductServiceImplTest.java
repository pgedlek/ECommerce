package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private CategoryRepository categoryRepositoryMock;
    @Mock
    private ModelMapper modelMapperMock;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void todo() {
        // given
        

        // when
        ProductResponse allProducts = productService.getAllProducts();

        // then
        assertThat(allProducts.getContent()).hasSize(0);
    }
}