package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.repository.AddressRepository;
import com.selimhorri.app.service.impl.AddressServiceImpl;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    @DisplayName("findAll returns list of addresses")
    void findAllReturnsList() {
        Address address = sampleAddress();
        when(addressRepository.findAll()).thenReturn(List.of(address));

        List<AddressDto> result = addressService.findAll();

        assertThat(result).hasSize(1);
        verify(addressRepository).findAll();
    }

    @Test
    @DisplayName("findById returns DTO when address exists")
    void findByIdReturnsDto() {
        Address address = sampleAddress();
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        AddressDto result = addressService.findById(1);

        assertThat(result.getAddressId()).isEqualTo(1);
        assertThat(result.getFullAddress()).isEqualTo("123 Main St");
        verify(addressRepository).findById(1);
    }

    @Test
    @DisplayName("findById throws when address not found")
    void findByIdThrowsWhenMissing() {
        when(addressRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.findById(99))
            .isInstanceOf(AddressNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save creates new address")
    void saveCreatesAddress() {
        Address address = sampleAddress();
        AddressDto dto = AddressDto.builder()
            .fullAddress("456 Oak Ave")
            .city("New York")
            .postalCode("10001")
            .build();
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.save(dto);

        assertThat(result).isNotNull();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("update updates existing address")
    void updateUpdatesAddress() {
        Address address = sampleAddress();
        AddressDto dto = AddressDto.builder()
            .addressId(1)
            .fullAddress("789 Pine St")
            .city("Chicago")
            .postalCode("60601")
            .build();
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.update(dto);

        assertThat(result).isNotNull();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("update with id updates existing address")
    void updateWithIdUpdatesAddress() {
        Address address = sampleAddress();
        AddressDto dto = AddressDto.builder()
            .fullAddress("789 Pine St")
            .city("Chicago")
            .postalCode("60601")
            .build();
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.update(1, dto);

        assertThat(result).isNotNull();
        verify(addressRepository).findById(1);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("deleteById removes address")
    void deleteByIdRemovesAddress() {
        addressService.deleteById(1);

        verify(addressRepository).deleteById(1);
    }

    private Address sampleAddress() {
        return Address.builder()
            .addressId(1)
            .fullAddress("123 Main St")
            .city("Springfield")
            .postalCode("62701")
            .build();
    }
}

