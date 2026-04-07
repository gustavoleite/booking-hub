package com.bookinghub.search.core.usecases;

import static org.mockito.Mockito.verify;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndexEstablishmentUseCaseTest {

    @Mock
    EstablishmentSearchRepository repository;
    @InjectMocks
    IndexEstablishmentUseCase useCase;

    @Test
    void shouldUpsertDocumentOnIndex() {
        var doc = EstablishmentDocument.builder().id("abc").name("Salão").build();
        useCase.execute(doc);
        verify(repository).upsert(doc);
    }
}
