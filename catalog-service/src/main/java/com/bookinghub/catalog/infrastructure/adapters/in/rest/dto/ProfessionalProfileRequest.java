package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProfessionalProfileRequest {
    private String name;
    private String bio;
    private String avatarUrl;
    private List<String> specialties;
}
