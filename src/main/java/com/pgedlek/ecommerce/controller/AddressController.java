package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.model.User;
import com.pgedlek.ecommerce.payload.AddressDTO;
import com.pgedlek.ecommerce.service.AddressService;
import com.pgedlek.ecommerce.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressController {
    private final AuthenticationUtil authenticationUtil;
    private final AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authenticationUtil.loggedInUser();
        AddressDTO createdAddressDTO = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(createdAddressDTO, CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        AddressDTO addressDto = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDto, OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddress() {
        User user = authenticationUtil.loggedInUser();
        List<AddressDTO> addresses = addressService.getUserAddresses(user);
        return new ResponseEntity<>(addresses, OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO addressDto = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(addressDto, OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        String status = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(status, OK);
    }

}
