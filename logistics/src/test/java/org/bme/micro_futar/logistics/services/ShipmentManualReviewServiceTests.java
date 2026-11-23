package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
