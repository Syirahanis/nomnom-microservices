package com.nomnom.deliveryservice.service;

import com.nomnom.deliveryservice.dto.RegisterRiderRequest;
import com.nomnom.deliveryservice.dto.RiderResponse;

import java.util.List;

public interface RiderService {

    RiderResponse registerRider(RegisterRiderRequest request);

    RiderResponse getRiderByCode(String riderCode);

    List<RiderResponse> getAllRiders();

}
