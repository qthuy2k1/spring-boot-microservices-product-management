package com.qthuy2k1.user.service;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.exception.UserNotFoundException;
import com.qthuy2k1.user.model.Role;
import com.qthuy2k1.user.model.UserModel;
import com.qthuy2k1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    private UserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserService(userRepository);
    }

    @Test
    void createUser() throws UserAlreadyExistsException {
        // given
        UserRequest user = new UserRequest("John Doe", "doe@gmail.com", "123123");

        // when
        underTest.createUser(user);

        // then
        ArgumentCaptor<UserModel> userArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        UserModel capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getName()).isEqualTo(user.getName());
        assertThat(capturedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(BCrypt.checkpw(user.getPassword(), capturedUser.getPassword())).isTrue();
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);

    }

    @Test()
    void createUser_WhenEmailIsTaken_ExceptionThrown() {
        // given
        UserRequest user = new UserRequest("John Doe", "doe@gmail.com", "123123");

        // when
        given(userRepository.existsByEmail(user.getEmail())).willReturn(true);

        // then
        assertThatThrownBy(() ->
                underTest.createUser(user)).
                hasMessageContaining("user already exists with email: " + user.getEmail());

        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers() {
        // when
        List<UserResponse> users = underTest.getAllUsers();

        // then
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser() throws UserNotFoundException {
        // given
        UserModel user = new UserModel(1, "John Doe", "doe@gmail.com", "123123", Role.USER);

        given(userRepository.findById(user.getId().toString())).willReturn(Optional.of(user));

        // when
        underTest.deleteUserById(user.getId().toString());

        // then
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserById_ExceptionThrown_UserNotFound() {
        // given
        String id = "1";
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() ->
                underTest.deleteUserById(id)).
                hasMessageContaining("user not found with ID: " + id);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateUserById() throws UserNotFoundException, UserAlreadyExistsException {
        // given
        UserModel existingUser = new UserModel(1, "John Doe", "doe@gmail.com", "123123", Role.USER);
        UserRequest updatedUser = new UserRequest("Updated Name", "updated.email@example.com", "456456");

        given(userRepository.findById(existingUser.getId().toString())).willReturn(Optional.of(existingUser));

        // when
        underTest.updateUserById(existingUser.getId().toString(), updatedUser);

        // then
        ArgumentCaptor<UserModel> userArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        UserModel capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getName()).isEqualTo(updatedUser.getName());
        assertThat(capturedUser.getEmail()).isEqualTo(updatedUser.getEmail());
        assertThat(BCrypt.checkpw(updatedUser.getPassword(), capturedUser.getPassword())).isTrue();
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void updateUserById_ExceptionThrown_UserNotFound() {
        // given
        String id = "1";
        UserRequest userRequest = new UserRequest("Updated Name", "updated.email@example.com", "456456");

        given(userRepository.existsByEmail(userRequest.getEmail())).willReturn(false);
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() ->
                underTest.updateUserById(id, userRequest)).
                hasMessageContaining("user not found with ID: " + id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserById_ExceptionThrown_EmailAlreadyIsTaken() {
        // given
        String id = "1";
        UserRequest userRequest = new UserRequest("Updated Name", "updated.email@example.com", "456456");

        given(userRepository.existsByEmail(userRequest.getEmail())).willReturn(true);

        // when
        // then
        assertThatThrownBy(() ->
                underTest.updateUserById(id, userRequest)).
                hasMessageContaining("user already exists with email: " + userRequest.getEmail());
        verify(userRepository, never()).save(any());
    }
}