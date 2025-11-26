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

    @Test
    @DisplayName("findAll returns list of users")
    void findAllReturnsList() {
        User user = sampleUser();
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("save creates new user")
    void saveCreatesUser() {
        User user = sampleUser();
        UserDto dto = UserDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@example.com")
            .build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.save(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update updates existing user")
    void updateUpdatesUser() {
        User user = sampleUser();
        UserDto dto = UserDto.builder()
            .userId(1)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@example.com")
            .build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update with id updates existing user")
    void updateWithIdUpdatesUser() {
        User user = sampleUser();
        UserDto dto = UserDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1, dto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("deleteById removes user")
    void deleteByIdRemovesUser() {
        userService.deleteById(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    @DisplayName("findByUsername returns user when exists")
    void findByUsernameReturnsUser() {
        User user = sampleUser();
        when(userRepository.findByCredentialUsername("john.doe")).thenReturn(Optional.of(user));

        UserDto result = userService.findByUsername("john.doe");

        assertThat(result.getUserId()).isEqualTo(1);
        verify(userRepository).findByCredentialUsername("john.doe");
    }

    @Test
    @DisplayName("findByUsername throws when user not found")
    void findByUsernameThrowsWhenMissing() {
        when(userRepository.findByCredentialUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("unknown");
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
