package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.service.impl.CredentialServiceImpl;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    @Test
    @DisplayName("findAll returns unmodifiable list of credentials")
    void findAllReturnsList() {
        Credential credential = sampleCredential();
        when(credentialRepository.findAll()).thenReturn(List.of(credential));

        List<CredentialDto> result = credentialService.findAll();

        assertThat(result).hasSize(1);
        verify(credentialRepository).findAll();
    }

    @Test
    @DisplayName("findById returns DTO when credential exists")
    void findByIdReturnsDto() {
        Credential credential = sampleCredential();
        when(credentialRepository.findById(1)).thenReturn(Optional.of(credential));

        CredentialDto result = credentialService.findById(1);

        assertThat(result.getCredentialId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("test.user");
        verify(credentialRepository).findById(1);
    }

    @Test
    @DisplayName("findById throws when credential not found")
    void findByIdThrowsWhenMissing() {
        when(credentialRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.findById(99))
            .isInstanceOf(CredentialNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findByUsername returns DTO when exists")
    void findByUsernameReturnsDto() {
        Credential credential = sampleCredential();
        when(credentialRepository.findByUsername("test.user")).thenReturn(Optional.of(credential));

        CredentialDto result = credentialService.findByUsername("test.user");

        assertThat(result.getUsername()).isEqualTo("test.user");
        verify(credentialRepository).findByUsername("test.user");
    }

    @Test
    @DisplayName("findByUsername throws when not found")
    void findByUsernameThrowsWhenMissing() {
        when(credentialRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.findByUsername("unknown"))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("save creates new credential")
    void saveCreatesCredential() {
        Credential credential = sampleCredential();
        CredentialDto dto = CredentialDto.builder()
            .username("new.user")
            .password("password")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .build();
        when(credentialRepository.save(org.mockito.ArgumentMatchers.any(Credential.class)))
            .thenReturn(credential);

        CredentialDto result = credentialService.save(dto);

        assertThat(result).isNotNull();
        verify(credentialRepository).save(org.mockito.ArgumentMatchers.any(Credential.class));
    }

    @Test
    @DisplayName("deleteById removes credential")
    void deleteByIdRemovesCredential() {
        credentialService.deleteById(1);

        verify(credentialRepository).deleteById(1);
    }

    private Credential sampleCredential() {
        return Credential.builder()
            .credentialId(1)
            .username("test.user")
            .password("hashed")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
    }
}

