package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.logistics.entities.Depo;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.exceptions.InvalidReferenceException;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CourierMapper;
import org.bme.micro_futar.logistics.mappers.DepoMapper;
import org.bme.micro_futar.logistics.mappers.DepoTransitMapper;
import org.bme.micro_futar.logistics.mappers.LocationCityMapper;
import org.bme.micro_futar.logistics.mappers.LocationCountryMapper;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.repositories.CourierRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Every create/update path rejects a bad reference before anything is saved or published; valid writes still go through. */
@ExtendWith(MockitoExtension.class)
class ReferenceValidationInServicesTests {

    @Mock private ReferenceValidator referenceValidator;
    @Mock private KafkaProducerService kafkaProducerService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private static InvalidReferenceException invalid() {
        return new InvalidReferenceException("someId", "Thing 42 (someId) does not exist or has been deleted");
    }

    // ---- LocationCountry

    @Mock private LocationCountryRepository countryRepository;
    @Mock private LocationCountryMapper countryMapper;
    @Mock private org.bme.micro_futar.logistics.repositories.CurrencyRepository currencyRepository;

    private LocationCountryService countryService() {
        return new LocationCountryService(countryRepository, countryMapper, kafkaProducerService, referenceValidator);
    }

    @Test
    void createCountryRejectsInvalidReference() {
        LocationCountryDTO dto = new LocationCountryDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> countryService().createCountry(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(countryRepository, never()).save(any());
        verifyNoInteractions(kafkaProducerService);
    }

    @Test
    void createCountryDefaultsCurrencyThenValidates() {
        LocationCountryDTO dto = new LocationCountryDTO();
        LocationCountry entity = new LocationCountry();
        when(countryMapper.toEntity(dto)).thenReturn(entity);
        when(countryRepository.save(entity)).thenReturn(entity);
        when(countryMapper.toDTO(entity)).thenReturn(dto);

        countryService().createCountry(dto);

        assertThat(dto.getCurrencyCode()).isEqualTo("HUF");
        verify(referenceValidator).validate(dto);
        verify(countryRepository).save(entity);
    }

    @Test
    void updateCountryRejectsInvalidReference() {
        LocationCountryDTO dto = new LocationCountryDTO();
        when(countryRepository.findById(1L)).thenReturn(Optional.of(new LocationCountry()));
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> countryService().updateCountry(1L, dto)).isInstanceOf(InvalidReferenceException.class);
        verify(countryRepository, never()).save(any());
    }

    @Test
    void updateCountryOfUnknownIdStaysEmptyWithoutValidation() {
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(countryService().updateCountry(1L, new LocationCountryDTO())).isEmpty();
        verifyNoInteractions(referenceValidator);
    }

    // ---- LocationCity

    @Mock private LocationCityRepository cityRepository;
    @Mock private LocationCityMapper cityMapper;

    private LocationCityService cityService() {
        return new LocationCityService(cityRepository, cityMapper, kafkaProducerService, referenceValidator);
    }

    @Test
    void createCityRejectsInvalidReference() {
        LocationCityDTO dto = new LocationCityDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> cityService().createCity(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(cityRepository, never()).save(any());
    }

    @Test
    void createCitySavesWhenReferencesAreValid() {
        LocationCityDTO dto = new LocationCityDTO();
        LocationCity entity = new LocationCity();
        when(cityMapper.toEntity(dto)).thenReturn(entity);
        when(cityRepository.save(entity)).thenReturn(entity);
        when(cityMapper.toDTO(entity)).thenReturn(dto);
        cityService().createCity(dto);
        verify(cityRepository).save(entity);
    }

    @Test
    void updateCityRejectsInvalidReference() {
        LocationCityDTO dto = new LocationCityDTO();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(new LocationCity()));
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> cityService().updateCity(1L, dto)).isInstanceOf(InvalidReferenceException.class);
        verify(cityRepository, never()).save(any());
    }

    @Test
    void updateCitySavesWhenReferencesAreValid() {
        LocationCityDTO dto = new LocationCityDTO();
        LocationCity entity = new LocationCity();
        when(cityRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(cityMapper.toEntity(dto)).thenReturn(entity);
        when(cityRepository.save(entity)).thenReturn(entity);
        when(cityMapper.toDTO(entity)).thenReturn(dto);
        assertThat(cityService().updateCity(1L, dto)).isPresent();
    }

    // ---- Depo

    @Mock private DepoRepository depoRepository;
    @Mock private DepoMapper depoMapper;

    private DepoService depoService() {
        return new DepoService(depoRepository, depoMapper, kafkaProducerService, eventPublisher, referenceValidator);
    }

    @Test
    void createDepoRejectsInvalidReference() {
        DepoDTO dto = new DepoDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> depoService().createDepo(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(depoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, kafkaProducerService);
    }

    @Test
    void createDepoSavesWhenReferencesAreValid() {
        DepoDTO dto = new DepoDTO();
        Depo entity = new Depo();
        when(depoMapper.toEntity(dto)).thenReturn(entity);
        when(depoRepository.save(entity)).thenReturn(entity);
        when(depoMapper.toDTO(entity)).thenReturn(dto);
        depoService().createDepo(dto);
        verify(depoRepository).save(entity);
    }

    @Test
    void updateDepoRejectsInvalidReference() {
        DepoDTO dto = new DepoDTO();
        when(depoRepository.findById(1L)).thenReturn(Optional.of(new Depo()));
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> depoService().updateDepo(1L, dto)).isInstanceOf(InvalidReferenceException.class);
        verify(depoRepository, never()).save(any());
    }

    @Test
    void updateDepoSavesWhenReferencesAreValid() {
        DepoDTO dto = new DepoDTO();
        Depo entity = new Depo();
        when(depoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(depoMapper.toEntity(dto)).thenReturn(entity);
        when(depoRepository.save(entity)).thenReturn(entity);
        when(depoMapper.toDTO(entity)).thenReturn(dto);
        assertThat(depoService().updateDepo(1L, dto)).isPresent();
    }

    // ---- DepoTransit

    @Mock private DepoTransitRepository transitRepository;
    @Mock private DepoTransitMapper transitMapper;

    private DepoTransitService transitService() {
        return new DepoTransitService(transitMapper, transitRepository, eventPublisher, referenceValidator);
    }

    @Test
    void createTransitRejectsInvalidReference() {
        DepoTransitDTO dto = new DepoTransitDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> transitService().createDepoTransit(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(transitRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createTransitSavesWhenReferencesAreValid() {
        DepoTransitDTO dto = new DepoTransitDTO();
        DepoTransit entity = new DepoTransit();
        when(transitMapper.toEntity(dto)).thenReturn(entity);
        when(transitRepository.save(entity)).thenReturn(entity);
        when(transitMapper.toDTO(entity)).thenReturn(dto);
        transitService().createDepoTransit(dto);
        verify(transitRepository).save(entity);
    }

    @Test
    void updateTransitRejectsInvalidReference() {
        DepoTransitDTO dto = new DepoTransitDTO();
        when(transitRepository.findById(1L)).thenReturn(Optional.of(new DepoTransit()));
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> transitService().updateDepoTransit(1L, dto)).isInstanceOf(InvalidReferenceException.class);
        verify(transitRepository, never()).save(any());
    }

    @Test
    void updateTransitSavesWhenReferencesAreValid() {
        DepoTransitDTO dto = new DepoTransitDTO();
        DepoTransit entity = new DepoTransit();
        when(transitRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transitMapper.toEntity(dto)).thenReturn(entity);
        when(transitRepository.save(entity)).thenReturn(entity);
        when(transitMapper.toDTO(entity)).thenReturn(dto);
        assertThat(transitService().updateDepoTransit(1L, dto)).isPresent();
    }

    // ---- Courier

    @Mock private CourierRepository courierRepository;
    @Mock private CourierMapper courierMapper;

    private CourierService courierService() {
        return new CourierService(courierRepository, courierMapper, kafkaProducerService, referenceValidator);
    }

    @Test
    void createCourierRejectsInvalidReference() {
        CourierDTO dto = new CourierDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> courierService().createCourier(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(courierRepository, never()).save(any());
    }

    @Test
    void createCourierSavesWhenReferencesAreValid() {
        CourierDTO dto = new CourierDTO();
        Courier entity = new Courier();
        when(courierMapper.toEntity(dto)).thenReturn(entity);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierMapper.toDTO(entity)).thenReturn(dto);
        courierService().createCourier(dto);
        verify(courierRepository).save(entity);
    }

    @Test
    void updateCourierRejectsInvalidReference() {
        CourierDTO dto = new CourierDTO();
        when(courierRepository.findById(1L)).thenReturn(Optional.of(new Courier()));
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> courierService().updateCourier(1L, dto)).isInstanceOf(InvalidReferenceException.class);
        verify(courierRepository, never()).save(any());
    }

    @Test
    void updateCourierSavesWhenReferencesAreValid() {
        CourierDTO dto = new CourierDTO();
        Courier entity = new Courier();
        when(courierRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(courierMapper.toEntity(dto)).thenReturn(entity);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierMapper.toDTO(entity)).thenReturn(dto);
        assertThat(courierService().updateCourier(1L, dto)).isPresent();
    }

    // ---- ShipmentRouteCourier

    @Mock private ShipmentRouteCourierRepository assignmentRepository;
    @Mock private ShipmentRouteCourierMapper assignmentMapper;

    private ShipmentRouteCourierService assignmentService() {
        return new ShipmentRouteCourierService(eventPublisher, kafkaProducerService, assignmentMapper,
                assignmentRepository, referenceValidator, null, null);
    }

    @Test
    void saveAssignmentRejectsInvalidReference() {
        ShipmentRouteCourierDTO dto = new ShipmentRouteCourierDTO();
        doThrow(invalid()).when(referenceValidator).validate(dto);
        assertThatThrownBy(() -> assignmentService().save(dto)).isInstanceOf(InvalidReferenceException.class);
        verify(assignmentRepository, never()).save(any());
        verifyNoInteractions(kafkaProducerService);
    }

    @Test
    void saveAssignmentSavesWhenReferencesAreValid() {
        ShipmentRouteCourierDTO dto = new ShipmentRouteCourierDTO();
        ShipmentRouteCourier entity = new ShipmentRouteCourier();
        when(assignmentMapper.toEntity(dto)).thenReturn(entity);
        when(assignmentRepository.save(entity)).thenReturn(entity);
        when(assignmentMapper.toDTO(entity)).thenReturn(dto);
        assignmentService().save(dto);
        verify(assignmentRepository).save(entity);
        verify(kafkaProducerService).sendShipmentRouteCourier(dto);
    }
}
