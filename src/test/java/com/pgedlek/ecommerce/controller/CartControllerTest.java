package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.model.Cart;
import com.pgedlek.ecommerce.payload.CartDTO;
import com.pgedlek.ecommerce.repository.CartRepository;
import com.pgedlek.ecommerce.service.CartService;
import com.pgedlek.ecommerce.util.AuthenticationUtil;
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
class CartControllerTest {

    @Mock
    private CartService cartServiceMock;

    @Mock
    private CartRepository cartRepositoryMock;

    @Mock
    private AuthenticationUtil authenticationUtilMock;

    @InjectMocks
    private CartController cartController;

    @Test
    void testAddProductToCart_Success() {
        // given
        Long productId = 1L;
        Integer quantity = 2;
        CartDTO cartDTO = new CartDTO();
        when(cartServiceMock.addProductToCart(productId, quantity)).thenReturn(cartDTO);

        // when
        ResponseEntity<CartDTO> response = cartController.addProductToCart(productId, quantity);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(cartDTO);
        verify(cartServiceMock).addProductToCart(productId, quantity);
    }

    @Test
    void testGetCarts_Success() {
        // given
        List<CartDTO> cartDTOS = List.of(new CartDTO());
        when(cartServiceMock.getAllCarts()).thenReturn(cartDTOS);

        // when
        ResponseEntity<List<CartDTO>> response = cartController.getCarts();

        // then
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(cartDTOS);
        verify(cartServiceMock).getAllCarts();
    }

    @Test
    void testGetCartById_Success() {
        // given
        String email = "test@example.com";
        Long cartId = 1L;
        Cart cart = new Cart();
        cart.setCartId(cartId);
        CartDTO cartDTO = new CartDTO();

        when(authenticationUtilMock.loggedInEmail()).thenReturn(email);
        when(cartRepositoryMock.findCartByEmail(email)).thenReturn(cart);
        when(cartServiceMock.getCart(email, cartId)).thenReturn(cartDTO);

        // when
        ResponseEntity<CartDTO> response = cartController.getCartById();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(cartDTO);
        verify(authenticationUtilMock).loggedInEmail();
        verify(cartRepositoryMock).findCartByEmail(email);
        verify(cartServiceMock).getCart(email, cartId);
    }

    @Test
    void testUpdateCartProduct_Success() {
        // given
        Long productId = 1L;
        String operation = "increment";
        CartDTO cartDTO = new CartDTO();

        when(cartServiceMock.updateProductQuantityInCart(productId, 1)).thenReturn(cartDTO);

        // when
        ResponseEntity<CartDTO> response = cartController.updateCartProduct(productId, operation);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(cartDTO);
        verify(cartServiceMock).updateProductQuantityInCart(productId, 1);
    }

    @Test
    void testUpdateCartProduct_DeleteOperation() {
        // given
        Long productId = 1L;
        String operation = "delete";
        CartDTO cartDTO = new CartDTO();

        when(cartServiceMock.updateProductQuantityInCart(productId, -1)).thenReturn(cartDTO);

        // when
        ResponseEntity<CartDTO> response = cartController.updateCartProduct(productId, operation);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(cartDTO);
        verify(cartServiceMock).updateProductQuantityInCart(productId, -1);
    }

    @Test
    void testDeleteProductFromCart_Success() {
        // given
        Long cartId = 1L;
        Long productId = 1L;
        String status = "Product removed from cart";

        when(cartServiceMock.deleteProductFromCart(cartId, productId)).thenReturn(status);

        // when
        ResponseEntity<String> response = cartController.deleteProductFromCart(cartId, productId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo(status);
        verify(cartServiceMock).deleteProductFromCart(cartId, productId);
    }
}