package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentManualReviewServiceTests {

    @Mock
    private ShipmentManualReviewRepository repository;

    @InjectMocks
    private ShipmentManualReviewService service;

    @Test
    void flagsAShipmentOnce() {
        when(repository.existsById(7L)).thenReturn(false, true);

        service.flag(7L, "first");
        service.flag(7L, "second");

        verify(repository, times(1)).save(any(ShipmentManualReview.class));
        verify(repository).save(argThat((ShipmentManualReview review) ->
                review.getShipmentId().equals(7L) && review.getReason().equals("first")
                        && review.getFlaggedAt() != null));
    }

    @Test
    void doesNotOverwriteAnExistingFlag() {
        when(repository.existsById(7L)).thenReturn(true);

        service.flag(7L, "again");

        verify(repository, never()).save(any());
    }

    @Test
    void resolveClosesAnOpenItemOnce() {
        ShipmentManualReview review = new ShipmentManualReview();
        review.setShipmentId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(review));

        assertThat(service.resolve(7L, "admin@example.com")).isTrue();
        var firstResolvedAt = review.getResolvedAt();
        assertThat(service.resolve(7L, "other@example.com")).isTrue();

        assertThat(firstResolvedAt).isNotNull();
        assertThat(review.getResolvedAt()).isEqualTo(firstResolvedAt);
        assertThat(review.getResolvedBy()).isEqualTo("admin@example.com");
        verify(repository, times(1)).save(review);
    }

    @Test
    void resolveReturnsFalseForUnknownShipment() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThat(service.resolve(9L, "admin")).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void aResolvedItemIsNotReopenedByAFurtherFlag() {
        when(repository.existsById(7L)).thenReturn(true);

        service.flag(7L, "redelivered message");

        verify(repository, never()).save(any());
    }
}
