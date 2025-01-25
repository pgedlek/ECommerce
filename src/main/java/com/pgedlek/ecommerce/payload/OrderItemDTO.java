package com.pgedlek.ecommerce.payload;

import com.pgedlek.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private Product product;
    private Integer quantity;
    private double discount;
    private double orderedProductPrice;
}
