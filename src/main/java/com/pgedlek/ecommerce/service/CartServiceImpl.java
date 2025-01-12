package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.exception.ApiException;
import com.pgedlek.ecommerce.exception.ResourceNotFoundException;
import com.pgedlek.ecommerce.model.Cart;
import com.pgedlek.ecommerce.model.CartItem;
import com.pgedlek.ecommerce.model.Product;
import com.pgedlek.ecommerce.payload.CartDTO;
import com.pgedlek.ecommerce.payload.ProductDTO;
import com.pgedlek.ecommerce.repository.CartItemRepository;
import com.pgedlek.ecommerce.repository.CartRepository;
import com.pgedlek.ecommerce.repository.ProductRepository;
import com.pgedlek.ecommerce.util.AuthenticationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AuthenticationUtil authenticationUtil;
    private final ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(productId, "productId", "Product"));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem != null) {
            throw new ApiException("Product " + product.getProductName() + " already exists in cart!");
        }

        if (product.getQuantity() == 0) {
            throw new ApiException("Product " + product.getProductName() + " is out of stock!");
        }

        if (product.getQuantity() < quantity) {
            throw new ApiException("Product " + product.getProductName() + " only has " + product.getQuantity() + " in stock!");
        }

        CartItem newCartItem = new CartItem(product, cart, quantity, product.getDiscount(), product.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        List<ProductDTO> productDTOList = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                })
                .toList();
        cartDTO.setProducts(productDTOList);

        return cartDTO;
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authenticationUtil.loggedInEmail());

        if (userCart != null) {
            return userCart;
        }

        return cartRepository.save(new Cart(0.0, authenticationUtil.loggedInUser()));
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new ApiException("No cart exists");
        }

        return carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> productDTOS = cart.getCartItems().stream()
                    .map(product -> modelMapper.map(product, ProductDTO.class))
                    .toList();
            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();
    }

    @Override
    public CartDTO getCart(String email, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(email, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException(cartId, "cartId", "Cart");
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));
        List<ProductDTO> productDTOS = cart.getCartItems().stream()
                .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email = authenticationUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);

        Cart cart = cartRepository.findById(userCart.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException(userCart.getCartId(), "cartId", "Cart"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(productId, "productId", "Product"));

        if (product.getQuantity() == 0) {
            throw new ApiException("Product " + product.getProductName() + " is out of stock!");
        }

        if (product.getQuantity() < quantity) {
            throw new ApiException("Product " + product.getProductName() + " only has " + product.getQuantity() + " in stock!");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem == null) {
            throw new ApiException("Product " + product.getProductName() + " no exists in cart!");
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new ApiException("Product " + product.getProductName() + " quantity cannot be negative!");
        }
        if (newQuantity == 0) {
            deleteProductFromCart(cart.getCartId(), productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(newQuantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        List<ProductDTO> productDTOList = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                })
                .toList();
        cartDTO.setProducts(productDTOList);

        return cartDTO;
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(cartId, "cartId", "Cart"));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem == null) {
            throw new ResourceNotFoundException(productId, "productId", "Product");
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        // Product product = cartItem.getProduct();
        // product.setQuantity(product.getQuantity() + cartItem.getQuantity());

        cartItemRepository.deleteCartItemByProductIdAndCartId(productId, cartId);

        return "Product " + cartItem.getProduct().getProductName() + " has been removed from cart";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(cartId, "cartId", "Cart"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(productId, "productId", "Product"));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem == null) {
            throw new ApiException("Product " + product.getProductName() + " no exists in cart!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);
    }
}
