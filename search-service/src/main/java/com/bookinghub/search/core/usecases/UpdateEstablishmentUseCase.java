package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class UpdateEstablishmentUseCase {

    private final EstablishmentSearchRepository repository;

    public void execute(String id, String name, String description, String city, String state, String zipCode, Double lat, Double lon) {
        log.info("Updating establishment {} in index", id);
        
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("description", description);
        fields.put("city", city);
        fields.put("state", state);
        fields.put("zipCode", zipCode);
        
        if (lat != null && lon != null) {
            Map<String, Double> geoPoint = new HashMap<>();
            geoPoint.put("lat", lat);
            geoPoint.put("lon", lon);
            fields.put("geoPoint", geoPoint);
        }
        
        repository.upsertPartial(id, fields);
    }
}
