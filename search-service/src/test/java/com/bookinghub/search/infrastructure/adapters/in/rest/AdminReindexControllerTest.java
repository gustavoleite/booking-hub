package com.bookinghub.search.infrastructure.adapters.in.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookinghub.search.core.usecases.ReindexUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminReindexController.class)
@ActiveProfiles("test")
class AdminReindexControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ReindexUseCase reindexUseCase;

    @Test
    void shouldReturn202WithIndexedCount() throws Exception {
        when(reindexUseCase.execute()).thenReturn(42);

        mockMvc.perform(post("/admin/reindex"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.indexed").value(42));

        verify(reindexUseCase).execute();
    }

    @Test
    void shouldReturn202EvenWhenNothingIndexed() throws Exception {
        when(reindexUseCase.execute()).thenReturn(0);

        mockMvc.perform(post("/admin/reindex"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.indexed").value(0));
    }
}
