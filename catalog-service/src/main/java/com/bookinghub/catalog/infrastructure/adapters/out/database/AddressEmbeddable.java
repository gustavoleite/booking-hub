package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {
  private String street;
  private String number;
  private String city;
  private String state;
  private String zipCode;
  private BigDecimal latitude;
  private BigDecimal longitude;
}
