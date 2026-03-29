package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class Address {
    private String street;
    private String number;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
