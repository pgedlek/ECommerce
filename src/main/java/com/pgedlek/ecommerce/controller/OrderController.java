package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.payload.OrderDTO;
import com.pgedlek.ecommerce.payload.OrderRequestDTO;
import com.pgedlek.ecommerce.service.OrderService;
import com.pgedlek.ecommerce.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        String email = authenticationUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(email,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage());
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
