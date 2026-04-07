package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProfessionalProfileRequest {
    private String name;
    private String bio;
    private String avatarUrl;
    private List<String> specialties;
}
