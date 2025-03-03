package com.pgedlek.ecommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private String username;
    private String email;
    private List<AddressDTO> addresses;
}
