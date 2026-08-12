package com.nomnom.deliveryservice.service;

import com.nomnom.deliveryservice.entity.Rider;
import com.nomnom.deliveryservice.entity.RiderStatus;
import com.nomnom.deliveryservice.exception.NoAvailableRiderException;
import com.nomnom.deliveryservice.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderAssignmentService {

    private final RiderRepository riderRepository;

    /**
     * find first available rider and give them delivery!!
     */
    public Rider assignAvailableRider() {

        List<Rider> availableRiders = riderRepository.findByStatus(RiderStatus.AVAILABLE);

        if (availableRiders.isEmpty()) {
            throw new NoAvailableRiderException(
                    "No available rider to assign to this order. Please register a rider first.");
        }

        Rider rider = availableRiders.get(0);
        rider.assign();
        return riderRepository.save(rider);
    }

    /**
     * return delivery back to pool and mark as available
     */
    public void releaseRider(String riderId) {

        riderRepository.findByRiderCode(riderId).ifPresent(rider -> {
            rider.release();
            riderRepository.save(rider);
        });
    }
}
