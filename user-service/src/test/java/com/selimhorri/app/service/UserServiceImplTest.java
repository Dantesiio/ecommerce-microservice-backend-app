package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("findById returns mapped DTO when user exists")
    void findByIdReturnsDto() {
        User user = sampleUser();
    when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1);

        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getCredentialDto().getUsername()).isEqualTo("john.doe");
        verify(userRepository).findById(1);
    }

    @Test
    @DisplayName("findById throws when user is not found")
    void findByIdMissingThrows() {
    when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("99");
    }

    private User sampleUser() {
        User user = User.builder()
            .userId(1)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .phone("123456789")
            .build();

        Credential credential = Credential.builder()
            .credentialId(10)
            .username("john.doe")
            .password("hashed")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .user(user)
            .build();

        user.setCredential(credential);
        return user;
    }
}
