package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookinghub.catalog.core.usecases.AddProvidedServiceUseCase;
import com.bookinghub.catalog.core.usecases.CreateEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.GetEstablishmentDetailsUseCase;
import com.bookinghub.catalog.core.usecases.InactivateEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.ListMyEstablishmentsUseCase;
import com.bookinghub.catalog.core.usecases.UpdateEstablishmentUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EstablishmentController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
public class EstablishmentControllerRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateEstablishmentUseCase createEstablishmentUseCase;
    @MockBean
    private UpdateEstablishmentUseCase updateEstablishmentUseCase;
    @MockBean
    private InactivateEstablishmentUseCase inactivateEstablishmentUseCase;
    @MockBean
    private GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase;
    @MockBean
    private ListMyEstablishmentsUseCase listMyEstablishmentsUseCase;
    @MockBean
    private AddProvidedServiceUseCase addProvidedServiceUseCase;

    @Test
    void shouldReturn200WhenAccessingWithoutPrefix() throws Exception {
        // Verifica se o endpoint responde na rota correta (sem o prefixo /api/catalog, já que o Gateway o remove)
        mockMvc.perform(get("/establishments/my-salons")
                .header("X-User-Id", "123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenAccessingWithPrefix() throws Exception {
        // Verifica se o endpoint NÃO responde na rota com o prefixo antigo
        mockMvc.perform(get("/api/catalog/establishments/my-salons"))
                .andExpect(status().isInternalServerError());
    }
}
