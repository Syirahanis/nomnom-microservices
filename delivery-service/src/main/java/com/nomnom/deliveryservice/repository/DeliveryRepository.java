package com.nomnom.deliveryservice.repository;

import com.nomnom.deliveryservice.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByDeliveryCode(String deliveryCode);

    Optional<Delivery> findByOrderId(String orderId);

}
