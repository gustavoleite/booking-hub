package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EstablishmentRequestTest {

    @Test
    void testGettersSetters() {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Name");
        request.setCnpj("CNPJ");
        request.setDescription("Desc");
        
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Street");
        address.setNumber("123");
        address.setCity("City");
        address.setState("State");
        address.setZipCode("Zip");
        request.setAddress(address);

        EstablishmentRequest.BusinessHourDto hour = new EstablishmentRequest.BusinessHourDto();
        hour.setDayOfWeek(1);
        hour.setOpenTime(LocalTime.MIN);
        hour.setCloseTime(LocalTime.MAX);
        request.setBusinessHours(List.of(hour));

        EstablishmentRequest.ProvidedServiceDto service = new EstablishmentRequest.ProvidedServiceDto();
        service.setTitle("Title");
        service.setDescription("Desc");
        request.setServices(List.of(service));

        assertEquals("Name", request.getName());
        assertEquals("CNPJ", request.getCnpj());
        assertEquals("Desc", request.getDescription());
        assertEquals(address, request.getAddress());
        assertEquals("Street", request.getAddress().getStreet());
        assertEquals("City", request.getAddress().getCity());
        assertEquals("State", request.getAddress().getState());
        assertEquals(1, request.getBusinessHours().size());
        assertEquals(1, request.getServices().size());
        assertEquals("Title", request.getServices().get(0).getTitle());
    }
}
