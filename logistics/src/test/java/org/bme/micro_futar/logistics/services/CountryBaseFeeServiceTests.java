package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.events.CountryBaseFeeChangedEvent;
import org.bme.micro_futar.logistics.exceptions.DuplicateEntityException;
import org.bme.micro_futar.logistics.mappers.CountryBaseFeeMapperImpl;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryBaseFeeServiceTests {

    @Mock private CountryBaseFeeRepository repository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ReferenceValidator referenceValidator;

    private CountryBaseFeeService service;

    @BeforeEach
    void setUp() {
        service = new CountryBaseFeeService(new CountryBaseFeeMapperImpl(), repository, eventPublisher, referenceValidator);
    }

    private static CountryBaseFeeDTO dto() {
        CountryBaseFeeDTO dto = new CountryBaseFeeDTO();
        dto.setCountryId(1L);
        dto.setPackageSizeId(2L);
        dto.setPrice(500.0);
        return dto;
    }

    private static CountryBaseFee entity(long id) {
        CountryBaseFee fee = new CountryBaseFee();
        fee.setId(id);
        fee.setCountryId(1L);
        fee.setPackageSizeId(2L);
        fee.setPrice(500.0);
        return fee;
    }

    @Test
    void createPublishesChangeEvent() {
        when(repository.findByCountryIdAndPackageSizeId(1L, 2L)).thenReturn(Optional.empty());
        when(repository.save(any(CountryBaseFee.class))).thenAnswer(i -> {
            CountryBaseFee saved = i.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        CountryBaseFeeDTO created = service.createCountryBaseFee(dto());

        assertThat(created.getId()).isEqualTo(5L);
        ArgumentCaptor<CountryBaseFeeChangedEvent> event = ArgumentCaptor.forClass(CountryBaseFeeChangedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().getChangeType()).isEqualTo(CountryBaseFeeChangedEvent.ChangeType.CREATED);
    }

    @Test
    void createRejectsDuplicateCountryAndPackageSize() {
        when(repository.findByCountryIdAndPackageSizeId(1L, 2L)).thenReturn(Optional.of(entity(9L)));

        assertThatThrownBy(() -> service.createCountryBaseFee(dto())).isInstanceOf(DuplicateEntityException.class);
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void updateOfTheSameRowKeepsItsOwnKey() {
        when(repository.existsById(9L)).thenReturn(true);
        when(repository.findByCountryIdAndPackageSizeId(1L, 2L)).thenReturn(Optional.of(entity(9L)));
        when(repository.save(any(CountryBaseFee.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.updateCountryBaseFee(9L, dto())).isPresent();
        verify(eventPublisher).publishEvent(any(CountryBaseFeeChangedEvent.class));
    }

    @Test
    void updateRejectsKeyOfAnotherRow() {
        when(repository.existsById(9L)).thenReturn(true);
        when(repository.findByCountryIdAndPackageSizeId(1L, 2L)).thenReturn(Optional.of(entity(8L)));

        assertThatThrownBy(() -> service.updateCountryBaseFee(9L, dto())).isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void updateOfMissingRowPublishesNothing() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThat(service.updateCountryBaseFee(9L, dto())).isEmpty();
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void deletePublishesChangeEventOnlyWhenRowExists() {
        when(repository.existsById(9L)).thenReturn(true);
        when(repository.existsById(10L)).thenReturn(false);

        assertThat(service.deleteCountryBaseFee(9L)).isTrue();
        assertThat(service.deleteCountryBaseFee(10L)).isFalse();
        verify(repository).deleteById(9L);
        verify(eventPublisher, org.mockito.Mockito.times(1)).publishEvent(any(CountryBaseFeeChangedEvent.class));
    }
}
