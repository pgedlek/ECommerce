package com.pgedlek.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @Size(min = 5, message = "Building name must be at least 5 characters")
    private String buildingName;

    @Size(min = 4, message = "City name must be at least 4 characters")
    private String city;

    @Size(min = 2, message = "State name must be at least 2 characters")
    private String state;

    @Size(min = 2, message = "Country name must be at least 2 characters")
    private String country;

    @Size(min = 6, message = "Pin code name must be at least 6 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String pincode, String state, String country, String city, String buildingName, String street) {
        this.pincode = pincode;
        this.state = state;
        this.country = country;
        this.city = city;
        this.buildingName = buildingName;
        this.street = street;
    }
}
