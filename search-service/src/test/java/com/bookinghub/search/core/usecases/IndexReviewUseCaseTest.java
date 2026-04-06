package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexReviewUseCaseTest {

    @Mock EstablishmentSearchRepository repository;
    @InjectMocks IndexReviewUseCase useCase;

    @Test
    void shouldCalculateAverageRatingOnFirstReview() {
        var doc = EstablishmentDocument.builder().id("est1").ratingSum(0.0).totalReviews(0).build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        useCase.execute("est1", 5.0);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        Map<String, Object> fields = captor.getValue();
        assertThat(fields.get("totalReviews")).isEqualTo(1);
        assertThat(fields.get("averageRating")).isEqualTo(5.0);
    }

    @Test
    void shouldCalculateCorrectAverageOnSecondReview() {
        var doc = EstablishmentDocument.builder().id("est1").ratingSum(5.0).totalReviews(1).build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        useCase.execute("est1", 3.0);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        Map<String, Object> fields = captor.getValue();
        assertThat(fields.get("totalReviews")).isEqualTo(2);
        assertThat((Double) fields.get("averageRating")).isEqualTo(4.0);
    }

    @Test
    void shouldSkipWhenEstablishmentNotFound() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());
        useCase.execute("unknown", 5.0);
        verify(repository, never()).upsertPartial(anyString(), any());
    }

    @Test
    void shouldSkipWhenRatingIsNull() {
        useCase.execute("est1", null);
        verify(repository, never()).findById(anyString());
    }
}
