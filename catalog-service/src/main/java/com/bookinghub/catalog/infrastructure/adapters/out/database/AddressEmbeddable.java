package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {
    private String street;
    private String number;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
