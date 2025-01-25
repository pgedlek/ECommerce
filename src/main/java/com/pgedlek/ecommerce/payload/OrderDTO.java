package com.pgedlek.ecommerce.payload;

import com.pgedlek.ecommerce.model.OrderItem;
import com.pgedlek.ecommerce.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String email;
    private List<OrderItem> orderItems;
    private LocalDate orderDate;
    private Payment payment;
    private Double totalAmount;
    private String orderStatus;
    private Long addressId;
}
