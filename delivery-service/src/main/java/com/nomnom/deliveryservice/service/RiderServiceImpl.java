package com.nomnom.deliveryservice.service;

import com.nomnom.deliveryservice.dto.RegisterRiderRequest;
import com.nomnom.deliveryservice.dto.RiderResponse;
import com.nomnom.deliveryservice.entity.Rider;
import com.nomnom.deliveryservice.exception.DuplicateResourceException;
import com.nomnom.deliveryservice.exception.ResourceNotFoundException;
import com.nomnom.deliveryservice.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiderServiceImpl implements RiderService {

    private final RiderRepository riderRepository;

    @Override
    public RiderResponse registerRider(RegisterRiderRequest request) {

        String riderCode = generateRiderCode();

        if (riderRepository.findByRiderCode(riderCode).isPresent()) {
            throw new DuplicateResourceException("Rider already exists with code: " + riderCode);
        }

        Rider rider = Rider.register(
                riderCode,
                request.getName(),
                request.getPhoneNumber());

        return toResponse(riderRepository.save(rider));
    }

    private String generateRiderCode() {
        return "RID" + System.currentTimeMillis();
    }

    @Override
    public RiderResponse getRiderByCode(String riderCode) {
        return toResponse(findRiderOrThrow(riderCode));
    }

    @Override
    public List<RiderResponse> getAllRiders() {
        return riderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Rider findRiderOrThrow(String riderCode) {
        return riderRepository.findByRiderCode(riderCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rider not found with code: " + riderCode));
    }

    private RiderResponse toResponse(Rider rider) {

        RiderResponse response = new RiderResponse();

        response.setId(rider.getId());
        response.setRiderCode(rider.getRiderCode());
        response.setName(rider.getName());
        response.setPhoneNumber(rider.getPhoneNumber());
        response.setStatus(rider.getStatus());
        response.setRegisteredAt(rider.getRegisteredAt());

        return response;
    }
}
