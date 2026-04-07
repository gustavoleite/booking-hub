package com.bookinghub.search.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.CatalogClient;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReindexUseCaseTest {

  @Mock
  CatalogClient catalogClient;
  @Mock
  EstablishmentSearchRepository repository;
  @InjectMocks
  ReindexUseCase useCase;

  @Test
  void shouldFetchFromCatalogAndUpsertAll() {
    var doc1 = EstablishmentDocument.builder().id("1").name("A").build();
    var doc2 = EstablishmentDocument.builder().id("2").name("B").build();
    when(catalogClient.fetchAllEstablishments()).thenReturn(List.of(doc1, doc2));

    int count = useCase.execute();

    assertThat(count).isEqualTo(2);
    verify(repository).upsert(doc1);
    verify(repository).upsert(doc2);
  }

  @Test
  void shouldReturnZeroWhenNothingToIndex() {
    when(catalogClient.fetchAllEstablishments()).thenReturn(List.of());
    int count = useCase.execute();
    assertThat(count).isZero();
    verify(repository, never()).upsert(any());
  }
}
