package com.qthuy2k1.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.dto.UserRequest;
import com.qthuy2k1.userservice.dto.UserResponse;
import com.qthuy2k1.userservice.exception.UserNotFoundException;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @InjectMocks
    private UserController userController;


    @Test
    void signup() throws Exception {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string("success"))
                .andDo(print());

        // then
        verify(userService).createUser(any());
    }

    @Test
    void signup_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        UserRequest userRequest = UserRequest.builder()
                .name("  ") // only whitespaces
                .email("doe.gmail") // invalid email address
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        String error = "{\"name\":\"your name shouldn't be blank\",\"email\":\"invalid email address\"}";
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(error))
                .andDo(print());

        // then
        verify(userService, never()).createUser(any());
    }

    @Test
    void getAllUsers() throws Exception {
        // given
        List<UserResponse> users = new ArrayList<>();
        users.add(
                new UserResponse(1, "John Doe", "johndoe@gmail.com", Role.USER)
        );
        users.add(
                new UserResponse(2, "Jane Doe", "janedoe@gmail.com", Role.USER)
        );
        given(userService.getAllUsers()).willReturn(users);

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(userService).getAllUsers();

        assertThat(result.getResponse().getContentAsString()).
                isEqualTo(objectMapper.writeValueAsString(users));
    }

    @Test
    void deleteUserById() throws Exception {
        // given
        Integer id = 1;

        // when
        mockMvc.perform(delete("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("success"))
                .andDo(print());

        // then
        verify(userService).deleteUserById(any());
    }


    @Test
    void updateUserById() throws Exception {
        // given
        Integer id = 1;
        UserRequest userRequest = UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isOk())
                .andExpect(content().string("success"))
                .andDo(print());

        // then
        verify(userService).updateUserById(Integer.valueOf(id), userRequest);
    }

    @Test
    void updateUserById_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        Integer id = 1;
        UserRequest userRequest = UserRequest.builder()
                .name("  ") // only whitespaces
                .email("doe.gmail") // invalid email address
                .password("123123")
                .build();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        String error = "{\"name\":\"your name shouldn't be blank\",\"email\":\"invalid email address\"}";
        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(error))
                .andDo(print());

        // then
        verify(userService, never()).updateUserById(any(), any());
    }

    @Test
    void getUserById() throws Exception {
        // given
        Integer id = 1;
        UserResponse userResponse = UserResponse.builder()
                .id(1)
                .name("John Doe")
                .email("doe@gmail.com")
                .role(Role.USER)
                .build();
        String userResponseString = objectMapper.writeValueAsString(userResponse);

        given(userService.getUserById(id)).willReturn(userResponse);

        // when
        mockMvc.perform(get("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(userResponseString))
                .andDo(print());

        // then
        verify(userService).getUserById(id);
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        // given
        Integer id = 1;

        given(userService.getUserById(id)).willThrow(new UserNotFoundException("user not found with ID: " + id));

        // when
        mockMvc.perform(get("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"user not found with ID: " + id + "\"}"))
                .andDo(print());

        // then
        verify(userService).getUserById(id);
    }

}