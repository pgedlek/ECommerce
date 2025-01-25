package com.pgedlek.ecommerce.service;

import com.pgedlek.ecommerce.payload.OrderDTO;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public OrderDTO placeOrder(String email, String paymentMethod) {
        return null;
    }
}
