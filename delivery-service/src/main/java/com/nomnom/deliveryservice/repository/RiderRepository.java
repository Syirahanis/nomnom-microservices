package com.nomnom.deliveryservice.repository;

import com.nomnom.deliveryservice.entity.Rider;
import com.nomnom.deliveryservice.entity.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {

    Optional<Rider> findByRiderCode(String riderCode);

    List<Rider> findByStatus(RiderStatus status);

}
