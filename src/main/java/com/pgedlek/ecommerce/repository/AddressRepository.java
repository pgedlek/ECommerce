package com.pgedlek.ecommerce.repository;

import com.pgedlek.ecommerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
