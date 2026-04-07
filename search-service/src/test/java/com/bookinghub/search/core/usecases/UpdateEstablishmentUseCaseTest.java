package com.bookinghub.search.core.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEstablishmentUseCaseTest {

  @Mock
  private EstablishmentSearchRepository repository;

  @InjectMocks
  private UpdateEstablishmentUseCase useCase;

  @Test
  void shouldUpdateEstablishmentSuccessfully() {
    String id = "id1";
    String name = "Name";
    String desc = "Desc";
    String city = "City";
    String state = "ST";
    String zip = "12345";
    Double lat = -23.5;
    Double lon = -46.6;

    useCase.execute(id, name, desc, city, state, zip, lat, lon);

    ArgumentCaptor<Map<String, Object>> fieldsCaptor = ArgumentCaptor.forClass(Map.class);
    verify(repository).upsertPartial(eq(id), fieldsCaptor.capture());

    Map<String, Object> fields = fieldsCaptor.getValue();
    assertEquals(name, fields.get("name"));
    assertEquals(desc, fields.get("description"));
    assertEquals(city, fields.get("city"));
    assertEquals(state, fields.get("state"));
    assertEquals(zip, fields.get("zipCode"));

    Map<String, Double> geoPoint = (Map<String, Double>) fields.get("geoPoint");
    assertEquals(lat, geoPoint.get("lat"));
    assertEquals(lon, geoPoint.get("lon"));
  }

  @Test
  void shouldUpdateWithoutGeoWhenLatLonIsNull() {
    String id = "id1";
    useCase.execute(id, "Name", "Desc", "City", "ST", "12345", null, null);

    ArgumentCaptor<Map<String, Object>> fieldsCaptor = ArgumentCaptor.forClass(Map.class);
    verify(repository).upsertPartial(eq(id), fieldsCaptor.capture());

    Map<String, Object> fields = fieldsCaptor.getValue();
    assertEquals(null, fields.get("geoPoint"));
  }
}
