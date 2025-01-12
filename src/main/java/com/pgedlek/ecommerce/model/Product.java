package com.pgedlek.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;
    private String image;

    @Size(min = 6, message = "Product description must contain at least 6 characters")
    private String description;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;

    public Product(Long productId, String productName, String image, String description, Integer quantity, double price, double discount, double specialPrice) {
        this.productId = productId;
        this.specialPrice = specialPrice;
        this.discount = discount;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.image = image;
        this.productName = productName;
    }

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private List<CartItem> products = new ArrayList<>();
}
