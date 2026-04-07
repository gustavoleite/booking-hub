package com.bookinghub.search.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchEstablishmentsUseCaseTest {

  @Mock
  EstablishmentSearchRepository repository;
  @InjectMocks
  SearchEstablishmentsUseCase useCase;

  @Test
  void shouldReturnResultsFromRepository() {
    var doc = EstablishmentDocument.builder().id("1").name("Salão").city("SP").state("SP").totalReviews(0).build();
    var result = SearchPage.EstablishmentResult.builder().document(doc).build();
    var page = SearchPage.builder().results(List.of(result)).totalHits(1).page(0).size(10).build();

    SearchFilter filter = SearchFilter.builder().city("SP").build();
    when(repository.search(eq(filter), eq(0), eq(10))).thenReturn(page);

    SearchPage response = useCase.execute(filter, 0, 10);
    assertThat(response.getTotalHits()).isEqualTo(1);
    assertThat(response.getResults()).hasSize(1);
  }

  @Test
  void shouldReturnEmptyWhenNoResults() {
    var emptyPage = SearchPage.builder().results(Collections.emptyList()).totalHits(0).page(0).size(10).build();
    SearchFilter filter = SearchFilter.builder().query("nada").build();
    when(repository.search(eq(filter), eq(0), eq(10))).thenReturn(emptyPage);

    SearchPage response = useCase.execute(filter, 0, 10);
    assertThat(response.getTotalHits()).isZero();
  }
}
