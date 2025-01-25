package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.payload.OrderDTO;

public interface OrderService {
    OrderDTO placeOrder(String email, String paymentMethod);
}
